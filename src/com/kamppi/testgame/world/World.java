package com.kamppi.testgame.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.kamppi.testgame.GamePanel;
import com.kamppi.testgame.Player;
import com.kamppi.testgame.graphics.Font;
import com.kamppi.testgame.tiles.CollisionType;
import com.kamppi.testgame.tiles.Tile;
import com.kamppi.testgame.util.KeyHandler;
import com.kamppi.testgame.util.MouseHandler;
import com.kamppi.testgame.util.Vector2d;

public class World {
	
	public WorldGenerator worldGen;
	public Player player;
	
	public World(int seed) {
		worldGen = new WorldGenerator(seed);
		player = new Player(this);
	}
	
	private List<Chunk> LOADED_CHUNKS = new ArrayList<>();
	
	public void unloadChunks(int playerChunkX) {
		for (Iterator<Chunk> i = LOADED_CHUNKS.iterator(); i.hasNext();) {
			Chunk chunk = i.next();
			if (Math.abs(chunk.getX() - playerChunkX) > 2) {
				i.remove();
			}
		}
	}
	
	public Chunk getOrCreateChunk(int x) {
		for (Chunk chunk : LOADED_CHUNKS) {
			if (chunk.getX() == x) {
				return chunk;
			}
		}
		
		Chunk chunk = new Chunk(x, this);
		worldGen.generateChunk(this, chunk);
		LOADED_CHUNKS.add(chunk);
		return chunk;
	}
	
	public boolean placeTile(int x, int y, Tile tile) {
		if ((getTile(x, y) != null && !getTile(x, y).isReplaceable()) || tile == null) return false;

		setTile(x, y, tile);
		return true;
	}
	
	public void destroyTile(int x, int y, int harvestLevel) {
		Tile tile = getTile(x, y);
		if (tile != null) {
			getTile(x, y).drop(this, harvestLevel);
			setTile(x, y, null);
		}
	}

	public void setTile(int x, int y, Tile tile) {
		if (y < 0 || y > 255) {
			return;
		}
		int chunkX = Math.floorDiv(x, 256);
		getOrCreateChunk(chunkX).setTile(Math.floorMod(x, 256), y, tile);
	}
	
	public Tile getTile(int x, int y) {
		if (y < 0 || y > 255) {
			return null;
		}
		int chunkX = Math.floorDiv(x, 256);
		return getOrCreateChunk(chunkX).getTile(Math.floorMod(x, 256), y);
	}
	
	public void update() {
		for (Chunk c : LOADED_CHUNKS) {
			for (int x = 0; x < 256; x++) {
				for (int y = 0; y < 256; y++) {
					if (c.getTile(x, y) != null) {
						Tile t = c.getTile(x, y);
						t.update(c, x, y);
					}
				}
			}
		}
		
		player.update();
		
		int playerChunkX = (int) Math.floor(player.getX() / 256);
		int loadChunkX = 0;
		int playerXMod = (int) (player.getX() % 256);
		if (playerXMod < 0) playerXMod += 256;
		if (playerXMod > 128) {
			loadChunkX = playerChunkX + 1;
		} else {
			loadChunkX = playerChunkX - 1;
		}
		
		getOrCreateChunk(playerChunkX);
		getOrCreateChunk(loadChunkX);
		
		unloadChunks(playerChunkX);

		cleanEntities();
	}
	
	public void input(MouseHandler mouse, KeyHandler key) {
		player.input(mouse, key);
	}
	
	public void render(Graphics2D g, Font font, MouseHandler mouse) {
		g.setColor(new Color(102, 178, 255));
		g.fillRect(0, 0, GamePanel.width, GamePanel.height);
		
		for (Chunk c : LOADED_CHUNKS) {
			for (int x = 0; x < 256; x++) {
				for (int y = 0; y < 256; y++) {
					if (c.getTile(x, y) != null) {
						Tile t = c.getTile(x, y);
						t.render(g, player, this, c, x, y);
						
//						g.setColor(Color.RED);
//						g.drawRect((int) (player.getRenderOffsetX() + (c.getX() * 256 + x - player.getX()) * 32), (int) (player.getRenderOffsetY() - (y - player.getY()) * 32), 32, 0);
						if (x == 0 || x == 255) {
							g.setColor(Color.RED);
							g.drawRect((int) (player.getRenderOffsetX() + (c.getX() * 256 + x - player.getX()) * 32 + 16), (int) (player.getRenderOffsetY() - (y - player.getY()) * 32 - 16), 0, 100);
						}
					}
				}
			}
		}
		
		player.render(g, font, mouse);
		
//		g.setColor(Color.WHITE);
//		g.drawLine(0, 0, GamePanel.width, GamePanel.height);
//		g.drawLine(0, GamePanel.height, GamePanel.width, 0);
	}
	
	public void cleanEntities() {
		for (Chunk c : LOADED_CHUNKS) {
			c.removeDeadEntities();
		}
	}

	public Vector2d collisionCheck(double x, double y, double width, double height) {
		int x1 = (int) Math.floor(x);
		int y1 = (int) Math.floor(y);
		int x2 = (int) Math.floor(x + width);
		int y2 = (int) Math.floor(y + height);
		if (x2 == x + width) x2--;
		if (y2 == y + height) y2--;
		
		for (int xTest = x1; xTest <= x2; xTest++) {
			for (int yTest = y1; yTest <= y2; yTest++) {
				if (getTile(xTest, yTest) != null && getTile(xTest, yTest).getCollision() == CollisionType.FULL_TILE) {
					return new Vector2d(Math.round(xTest), Math.round(yTest));
				}
			}
		}
		return null;
	}
	
	public Vector2d collisionCheck(double x, double y) {
		int xx = (int) Math.floor(x);
		int yy = (int) Math.floor(y);
		
		if (getTile(xx, yy) != null && getTile(xx, yy).getCollision() == CollisionType.FULL_TILE) {
			return new Vector2d(xx, yy);
		}
		return null;
	}
	
	public boolean isChunkLoaded(int x) {
		for (Chunk c : LOADED_CHUNKS) {
			if (c.getX() == x) return true;
		}
		return false;
	}
}
