package me.Dary.KriptonBotBungee.Managers.storage;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import lombok.Getter;
import me.Dary.KriptonBotBungee.KriptonBot;
import me.Dary.KriptonBotBungee.Objects.LevelsData;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.FileUpload;

public class LevelsManager {
	
	private KriptonBot plugin;
	private long loaded = System.currentTimeMillis();
	@Getter
	private Map<Long, LevelsData> data;
	@Getter
	private Map<Integer, LevelsData> leaderboard;
	@Getter
	FileUpload leaderboardIMG;
	@Getter
	private Map<Integer, Role> roleRewards = new HashMap<>();
	
	public LevelsManager(KriptonBot plugin) {
		this.plugin = plugin;	
		data = new HashMap<>();
		plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
		    @Override
		    public void run() {		    	
		    	saveData();
		    	updateLeaderboard();
		    	
		    }
		}, 5, 5, TimeUnit.MINUTES);
		
		loadAllData().thenRun(() -> {
			plugin.getLogger().info("[Niveles] Cargados "+data.size()+" perfiles en "+(System.currentTimeMillis()-loaded)+"ms");
			updateLeaderboard();
			
		}).exceptionally(ex -> {
		    ex.printStackTrace();
		    return null;
		});	
		roleRewards = Map.of(
				15, plugin.jda().getRoleById(1178870049684070530l),
				30, plugin.jda().getRoleById(1178869928477085746l),
				45, plugin.jda().getRoleById(1178869859979903006l),
				60, plugin.jda().getRoleById(1178869781135364126l));
	}
    public void updateLeaderboard() {
        Map<Long, LevelsData> stats = new TreeMap<>(
                Comparator.comparingInt(profile -> data.get(profile).getTotalXp())
        );
        stats.putAll(data);

        Map<Integer, LevelsData> leaderboard = new HashMap<>();
        int x = stats.size();
        for(LevelsData data : stats.values()) {   
        	data.boardPosition(x);
        	leaderboard.put(x, data);
        	x--;
        }
        this.leaderboard = leaderboard;
        this.leaderboardIMG = FileUpload.fromData(getLeaderboardIMG(), "leaderboard.png");
    }
	
	public CompletableFuture<Void> loadAllData() {
	    return CompletableFuture.runAsync(() -> {
	        try {
	            PreparedStatement ps = plugin.getSQL().connection.prepareStatement("SELECT * FROM levels;");
	            ResultSet result = ps.executeQuery();
	            while (result.next()) {
	                LevelsData stats = new LevelsData(
	                        result.getLong("ID"),
	                        result.getInt("Messages"),
	                        result.getInt("Level"),
	                        result.getInt("Experience"),
	                        result.getString("Color"),
	                        result.getString("Background"),
	                        result.getBoolean("LevelNotifications")
	                );
	                data.put(stats.discordID(), stats);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    });
	}
	
	public LevelsData getStats(long discord) {
	    if (data.containsKey(discord)) return data.get(discord);    
	    LevelsData result = select(discord).join();
	    if (result == null) {
	        result = new LevelsData(discord);
	        insert(result);
	    }
	    data.put(discord, result);
	    return result;
	}
	
    public void saveData() {
    	long time = System.currentTimeMillis();
    	plugin.getLogger().info("[Niveles] Guardando estadisticas...");
        data.values().forEach(this::update);
        plugin.getLogger().info("[Niveles] Guardadas en "+(System.currentTimeMillis()-time));
    }
    
    public void saveData(long discord) {
    	if(!data.containsKey(discord)) return;
        update(data.get(discord));
    }
    
    private CompletableFuture<LevelsData> select(long discord) {
    	
    	CompletableFuture future = new CompletableFuture();
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
        	LevelsData stats = null;
            try {                      	
                PreparedStatement ps = plugin.dataManager().connection.prepareStatement(
                		"SELECT * FROM `levels` WHERE ID=?;");     
                ps.setLong(1, discord);             
                ResultSet result = ps.executeQuery();
                if (result.next()) {               
                	stats = new LevelsData(                			
                			discord,
                    		result.getInt("Messages"),
                    		result.getInt("Level"),
                    		result.getInt("Experience"),
                    		result.getString("Color"),
                    		result.getString("Background"),
                    		result.getBoolean("LevelNotifications")
                    );
                }
                
            } catch(SQLException ex) {
            	plugin.getLogger().severe("Error al obtener la información de "+discord+"");
            	ex.printStackTrace();
            }
            future.complete(stats);
        });
        return future;
    }
    
    private void insert(LevelsData data) {
        try {
        	PreparedStatement ps = plugin.dataManager().connection.prepareStatement(
        			"INSERT INTO `levels` (ID, Messages, Level, Experience, Color, Background, LevelNotifications) VALUES (?, ?, ?, ?, ?, ?, ?);"
            );
        	ps.setLong(1, data.discordID());
       	 	ps.setInt(2, data.messages());
       	 	ps.setInt(3, data.level());
       	 	ps.setInt(4, data.experience());
       	 	ps.setString(5, data.color());
       	 	ps.setString(6, data.customBackground());
       	 	ps.setBoolean(7, data.levelNotifications());
            ps.executeUpdate();
            plugin.getLogger().info("[Niveles] Guardada la información de "+data.discordID());
        } catch(SQLException ex) {
        	plugin.getLogger().severe("Error al insertar la información de "+data.discordID());
        	ex.printStackTrace();
        }
        updateLeaderboard();
    }
    
    private void update(LevelsData data) {
    	if(data.saved()) return;
        try {   	
       	 	PreparedStatement ps = plugin.dataManager().connection.prepareStatement(
       	 			"UPDATE `levels` SET Messages=?, Level=?, Experience=?, Color=?, Background=?, LevelNotifications=? WHERE ID=?;");
       	 	ps.setInt(1, data.messages());
       	 	ps.setInt(2, data.level());
       	 	ps.setInt(3, data.experience());
       	 	ps.setString(4, data.color());
       	 	ps.setString(5, data.customBackground());
       	 	ps.setBoolean(6, data.levelNotifications());
       	 	ps.setLong(7, data.discordID());
            ps.executeUpdate();   
            data.saved(true);
        } catch(SQLException ex) {
        	plugin.getLogger().severe("Error al actualizar la información de "+data.discordID());
        	ex.printStackTrace();
        }      
    }
    
    private void delete(long discord) {
        try {
        	PreparedStatement ps = plugin.dataManager().connection.prepareStatement(
        			"DELETE FROM `levels` WHERE playerName=?;");
            ps.setLong(1, discord);
            ps.execute();
        } catch (SQLException ex) {
        	plugin.getLogger().severe("Error al borrar data de "+discord);
        	ex.printStackTrace();
        }
    }
    
    public InputStream image(LevelsData profile) {
    	try {
    		User user = profile.getUser();
            int width = 1000;
            int height = 250;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D g = (Graphics2D) image.getGraphics();
       
            if(profile.customBackground() != null) {
            	 g.drawImage(profile.background(), 0, 0, null);
            } else {
                g.setColor(new Color(35, 39, 42)); // Gris
                g.fillRect(0, 0, width, height); 
            }

            
            g.setColor(new Color(255, 255, 255)); //Blanco
            
            g.fill(new RoundRectangle2D.Double(12, 186, 750, 45, 40, 40));

            g.setColor(Color.decode(profile.getColor()));
            g.fillPolygon(new int[] {855, 1000, 1000, 670}, new int[] {250, 250, 0, 0}, 4);
            
            double progress = (700*profile.getLevelProgress()/100);
            g.fill(new RoundRectangle2D.Double(12, 186, 50+(progress > 750 ? 750 : progress), 45, 40, 40));
            if(progress < 5) {
                g.setColor(new Color(255, 255, 255)); //Blanco
                g.fillRect(40, 186, 50, 45);
                g.setColor(Color.decode(profile.getColor()));
            }
            
            g.fillRect(190, 100, 260, 5);

            g.fill(new Ellipse2D.Double(17, 17, 156, 156));
            String avatar = user.getAvatarUrl();
            g.drawImage(avatar(avatar != null ? avatar : user.getDefaultAvatarUrl()), 20, 20, null);
            
            //Dibujar nombre de usuario con tamaño ajustado
            g.setColor(Color.decode("#4d4d4d"));      
            String name = plugin.utils().getUserTag(user);
            g.setFont(new Font("SansSerif", Font.BOLD, 45));
            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(name);
            while (textWidth > 500) {
                int tamañoFuenteActual = g.getFont().getSize();
                g.setFont(new Font("SansSerif", Font.BOLD, tamañoFuenteActual - 1));
                metrics = g.getFontMetrics();
                textWidth = metrics.stringWidth(name);
            }

            for (int i = -1; i <= 1; i++) { //Borde de letra de 1px
                for (int j = -1; j <= 1; j++) {
                    g.drawString(name, 190 + i, 80 + j);
                }
            }

            g.setColor(Color.WHITE);
            g.drawString(name, 190, 80);
            //Dibujar información de nivel y experiencia
            g.setColor(Color.decode("#4d4d4d"));          
            String info = "Nivel: " + profile.level() + "       Rango: #" + profile.getBoardPosition();          
            g.setFont(new Font("Comic Sans", Font.BOLD, 35));

            for (int i = -1; i <= 1; i++) { //Borde de letra de 1px
                for (int j = -1; j <= 1; j++) {
                    g.drawString(info, 190 + i, 160 + j);
                }
            }

            g.setColor(Color.WHITE);
            g.drawString(info, 190, 160);
            
            g.setColor(new Color(35, 35, 35));
            g.setFont(new Font("Comic Sans", Font.ITALIC , 30));
            g.drawString(profile.experience()+" / "+profile.getExpRequired()+" EXP", 510, 220);
            g.dispose();
            
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            return new ByteArrayInputStream(imageBytes);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public InputStream getLeaderboardIMG() {
    	try {
            int width = 680;
            int height = 745;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
           
            Graphics2D g = (Graphics2D) image.getGraphics();
            
			for(int i = 1; i <= 10; i++) {
				LevelsData profile = leaderboard.get(i);
				User user = profile.getUser();
				BufferedImage card = new BufferedImage(680, 70, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = card.createGraphics();
				g2.setColor(new Color(35, 39, 42));
				g2.fill(new RoundRectangle2D.Double(0, 0, 680, 70, 18, 18));	
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				String avatar = user.getAvatarUrl();
				g2.drawImage(miniAvatar(avatar != null ? avatar : user.getDefaultAvatarUrl()), 0, 0, null);
				g2.setFont(new Font("Arial", Font.BOLD, 26));
				Color rankColor = profile.getBoardPosition() > 3 ? Color.WHITE : profile.getBoardPosition() == 3 ? new Color(205, 127, 50) : profile.getBoardPosition() == 2 ? new Color(167, 167, 173) : new Color(255, 215, 0);
				write(g2, 81, 45, 
						new String[]{"#"+profile.getBoardPosition(), " • ", plugin.utils().getUserTag(user), " • ", "LVL: "+profile.level()}, 
						new Color[]{rankColor, new Color(153, 170, 181), Color.WHITE, new Color(153, 170, 181), Color.WHITE});		
				g2.dispose();
				int y = i == 1 ? 0 : (i-1)*75;
				g.drawImage(card, 0, y, null);
			}

            g.dispose();
            
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            return new ByteArrayInputStream(imageBytes);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
	private BufferedImage miniAvatar(String url) {
    	try {
            BufferedImage imagenOriginal = ImageIO.read(new URL(url));      
            imagenOriginal = resize(imagenOriginal, 70, 70);
            BufferedImage imagenRedondeada = new BufferedImage(70, 70, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = imagenRedondeada.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setClip(new RoundRectangle2D.Float(0, 0, 70, 70, 18, 18));
            g2d.drawImage(imagenOriginal, 0, 0, null);
            g2d.dispose();
            return imagenRedondeada;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		return null;
    }
    
    private void write(Graphics2D g, int x, int y, String[] args, Color[] colors) {
    	int i = 0;
    	Color lastColor = g.getColor();
    	for(String key : args) {
    		Color color = i >= colors.length ? colors[colors.length-1] : colors[i];
    		g.setColor(color);
    		g.drawString(key, x, y);	
    		x += g.getFontMetrics().stringWidth(key) + 5;
    		i++;
    	}
    	g.setColor(lastColor);
    }
    private BufferedImage avatar(String url) {
        try {
            BufferedImage imagen = ImageIO.read(new URL(url));        
            int size = 150;
            imagen = resize(imagen, size, size);         
            BufferedImage circular = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = circular.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setClip(new Ellipse2D.Float(0, 0, size, size));
            g2d.drawImage(imagen, 0, 0, null);
            g2d.dispose();  
            return circular;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private BufferedImage resize(BufferedImage original, int width, int height) {
        BufferedImage redimensionada = new BufferedImage(width, height, original.getType());
        Graphics2D g = redimensionada.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, 0, 0, original.getWidth(), original.getHeight(), null);
        g.dispose();
        return redimensionada;
    }
    
}
