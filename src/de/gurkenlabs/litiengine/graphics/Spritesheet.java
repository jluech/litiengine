/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.tilemap.ITileset;
import de.gurkenlabs.util.io.FileUtilities;

public class Spritesheet {

  public static final List<Spritesheet> spritesheets = new CopyOnWriteArrayList<>();

  /** The path. */
  private final String path;
  
  private final String name;

  /** The rows. */
  private int rows;

  /** The sprite height. */
  private int spriteHeight;

  /** The sprites per row. */
  private int columns;

  /** The sprite width. */
  private int spriteWidth;

  private final int hashCode;

  private BufferedImage image;

  public static List<Spritesheet> load(final String spriteInfoFile) {
    final String COMMENT_CHAR = "#";
    
    ArrayList<Spritesheet> sprites = new ArrayList<>();
    InputStream fileStream = FileUtilities.getGameFile(spriteInfoFile);
    if (fileStream == null) {
      return sprites;
    }

    try (BufferedReader br = new BufferedReader(new InputStreamReader(fileStream))) {
      String line;
      int cnt = 0;
      while ((line = br.readLine()) != null) {
        
        if(line.isEmpty() || line.startsWith(COMMENT_CHAR)){
          continue;
        }
        
        List<String> items = Arrays.asList(line.split("\\s*,\\s*"));
        if (items.size() < 3) {
          continue;
        }

        try {
          String name = Game.getInfo().getSpritesDirectory() + items.get(0);

          int width = Integer.parseInt(items.get(1));
          int height = Integer.parseInt(items.get(2));

          sprites.add(load(name, width, height));
          cnt++;
        } catch (NumberFormatException nfe) {
          nfe.printStackTrace();
          continue;
        }
      }
      
      System.out.println(cnt + " spritesheets loaded from '" + spriteInfoFile + "'");
    } catch (IOException e) {
      e.printStackTrace();
    }

    return sprites;
  }

  public static Spritesheet load(final ITileset tileset) {
    Spritesheet found = find(tileset.getImage().getAbsoluteSourcePath());
    if (found != null) {
      return found;
    }

    Spritesheet sprite = new Spritesheet(tileset);
    return sprite;
  }

  public static Spritesheet load(final String path, final int spriteWidth, final int spriteHeight) {
    Spritesheet found = find(path);
    if (found != null) {
      return found;
    }

    Spritesheet sprite = new Spritesheet(path, spriteWidth, spriteHeight);
    return sprite;
  }

  public static Spritesheet load(final BufferedImage image, final String path, final int spriteWidth, final int spriteHeight) {
    Spritesheet found = find(path);
    if (found != null) {
      return found;
    }

    Spritesheet sprite = new Spritesheet(image, path, spriteWidth, spriteHeight);
    return sprite;
  }

  public static Spritesheet find(final String path) {
    if (path == null || path.isEmpty()) {
      return null;
    }
    
    String name = FileUtilities.getFileName(path);

    final Optional<Spritesheet> sheet = spritesheets.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findFirst();
    if (!sheet.isPresent()) {
      return null;
    }

    return sheet.get();
  }

  private Spritesheet(final BufferedImage image, final String path, final int spriteWidth, final int spriteHeight) {
    this.image = image;

    this.path = path;
    this.name = FileUtilities.getFileName(path);
    this.spriteWidth = spriteWidth;
    this.spriteHeight = spriteHeight;

    this.hashCode = this.getName().hashCode();
    this.updateRowsAndCols();
    spritesheets.add(this);
    System.out.println("added " + image.hashCode() + " (" + path + ")");
  }

  private Spritesheet(final ITileset tileset) {
    this(RenderEngine.getImage(tileset.getImage().getAbsoluteSourcePath()), tileset.getImage().getAbsoluteSourcePath(), tileset.getTileDimension().width, tileset.getTileDimension().height);
  }

  private Spritesheet(final String path, final int spriteWidth, final int spriteHeight) {
    this(RenderEngine.getImage(path), path, spriteWidth, spriteHeight);
  }

  public BufferedImage getImage() {
    return this.image != null ? this.image : RenderEngine.getImage(this.getPath());
  }

  /**
   * Gets the sprites per row.
   *
   * @return the sprites per row
   */
  public int getColumns() {
    return this.columns;
  }

  public String getPath() {
    return this.path != null ? this.path : "";
  }
  
  /**
   * The unique name of this spritesheet. A spritesheet can always be identified by this name within a game project.
   * @return The name of the spritesheet.
   */
  public String getName(){
    return this.name;
  }

  public int getRows() {
    return this.rows;
  }

  public BufferedImage getSprite(final int index) {
    final String imageCacheKey = MessageFormat.format("{0}_{1}", this.hashCode, index);
    if (ImageCache.SPRITES.containsKey(imageCacheKey)) {
      return ImageCache.SPRITES.get(imageCacheKey);
    }

    final BufferedImage bigImg = this.getImage();
    if (bigImg == null) {
      return null;
    }

    final Point position = this.getLocation(index);
    final BufferedImage smallImage = bigImg.getSubimage(position.x, position.y, this.spriteWidth, this.spriteHeight);
    ImageCache.SPRITES.putPersistent(imageCacheKey, smallImage);
    return smallImage;
  }

  /**
   * Gets the sprite height.
   *
   * @return the sprite height
   */
  public int getSpriteHeight() {
    return this.spriteHeight;
  }

  /**
   * Gets the sprite width.
   *
   * @return the sprite width
   */
  public int getSpriteWidth() {
    return this.spriteWidth;
  }

  /**
   * Gets the total sprites.
   *
   * @return the total sprites
   */
  public int getTotalNumberOfSprites() {
    return this.getRows() * this.getColumns();
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  public void setSpriteHeight(int spriteHeight) {
    this.spriteHeight = spriteHeight;
    this.updateRowsAndCols();
  }

  public void setSpriteWidth(int spriteWidth) {
    this.spriteWidth = spriteWidth;
    this.updateRowsAndCols();
  }

  private Point getLocation(final int index) {
    final int row = index / this.getColumns();
    final int column = index % this.getColumns();

    return new Point(column * this.getSpriteWidth(), row * this.getSpriteHeight());
  }

  private void updateRowsAndCols() {
    BufferedImage sprite = this.getImage();
    if (sprite != null && sprite.getWidth() != 0 && sprite.getHeight() != 0 && this.spriteWidth != 0 && this.spriteHeight != 0) {
      this.columns = sprite.getWidth() / this.spriteWidth;
      this.rows = sprite.getHeight() / this.spriteHeight;
    } else {
      this.columns = 0;
      this.rows = 0;
    }
  }
}