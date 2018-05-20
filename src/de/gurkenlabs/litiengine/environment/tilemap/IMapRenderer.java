package de.gurkenlabs.litiengine.environment.tilemap;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.graphics.RenderType;

public interface IMapRenderer {
  public BufferedImage getImage(IMap map, RenderType...renderTypes);

  /**
   * Gets the supported orientation.
   *
   * @return the supported orientation
   */
  public MapOrientation getSupportedOrientation();

  /**
   * Renders the entire map (without overlay layers) onto the specified graphics
   * object. The resulting map image of this method is cached and therefore
   * animation of tiles is not supported, using this method.
   *
   * @param g
   *          the graphics object
   * @param map
   *          the map
   */
  public void render(Graphics2D g, IMap map, RenderType...renderTypes);

  /**
   * Renders the entire map (without overlay layers) onto the specified graphics
   * object. The resulting map image of this method is cached and therefore
   * animation of tiles is not supported, using this method.
   *
   * @param g
   *          the graphics object
   * @param map
   *          the map
   * @param offsetX
   *          The horizontal offset in pixels for the map image that is applied
   *          when rendering on the graphics object.
   * @param offsetY
   *          The vertical offset in pixels for the map image that is applied
   *          when rendering on the graphics object.
   */
  public void render(Graphics2D g, IMap map, double offsetX, double offsetY, RenderType...renderTypes);

  /*
   * Renders all layers (without the overlay layers) of the specified map. The
   * viewport defines the region of the map that is about to be rendered.
   */
  public void render(Graphics2D g, IMap map, Rectangle2D viewport, RenderType...renderTypes);
}
