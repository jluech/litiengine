/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.util.MathUtilities;

/**
 * The Class Camera.
 */
public class Camera implements ICamera {
  private final List<Consumer<Float>> zoomChangedConsumer;
  /**
   * Provides the center location for the viewport.
   */
  private Point2D focus;

  /** The shake duration. */
  private int shakeDuration = 2;

  /** The shake intensity. */
  private double shakeIntensity = 1;

  /** The shake tick. */
  private long shakeTick;

  private int shakeDelay;
  private long lastShake;

  private double shakeOffsetX;
  private double shakeOffsetY;

  private float zoom;
  private int zoomDelay;
  private long zoomTick;
  private float zoomStep;

  private Rectangle2D viewPort;

  /**
   * Instantiates a new camera.
   */
  public Camera() {
    this.zoomChangedConsumer = new CopyOnWriteArrayList<>();
    this.focus = new Point2D.Double(0, 0);
    this.zoom = Game.getInfo().getRenderScale();
  }

  @Override
  public void update(IGameLoop loop) {
    if (Game.getScreenManager().getCamera() != null && !Game.getScreenManager().getCamera().equals(this)) {
      return;
    }

    if (this.zoom > 0 && Game.getInfo().getRenderScale() != this.zoom) {
      if (loop.getDeltaTime(this.zoomTick) >= this.zoomDelay) {
        Game.getInfo().setRenderScale(this.zoom);
        for (Consumer<Float> cons : this.zoomChangedConsumer) {
          cons.accept(this.zoom);
        }
        
        this.zoom = 0;
        this.zoomDelay = 0;
        this.zoomTick = 0;
        this.zoomStep = 0;
      } else {

        float newRenderScale = Game.getInfo().getRenderScale() + this.zoomStep;
        Game.getInfo().setRenderScale(newRenderScale);
        for (Consumer<Float> cons : this.zoomChangedConsumer) {
          cons.accept(newRenderScale);
        }
      }
    }

    if (!this.isShakeEffectActive()) {
      this.shakeOffsetX = 0;
      this.shakeOffsetY = 0;
      return;
    }

    if (loop.getDeltaTime(this.lastShake) > shakeDelay) {
      this.shakeOffsetX = this.getShakeIntensity() * MathUtilities.randomSign();
      this.shakeOffsetY = this.getShakeIntensity() * MathUtilities.randomSign();
      this.lastShake = loop.getTicks();
    }
  }

  @Override
  public Point2D getFocus() {
    return this.focus;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.graphics.ICamera#getMapLocation(java.awt.geom.Point2D)
   */
  @Override
  public Point2D getMapLocation(final Point2D viewPortLocation) {
    final double x = viewPortLocation.getX() - this.getPixelOffsetX();
    final double y = viewPortLocation.getY() - this.getPixelOffsetY();
    return new Point2D.Double(x, y);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getPixelOffsetX()
   */
  @Override
  public double getPixelOffsetX() {
    return this.getViewPortCenterX() - (this.getFocus() != null ? this.getFocus().getX() : 0);
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getPixelOffsetY()
   */
  @Override
  public double getPixelOffsetY() {
    return this.getViewPortCenterY() - (this.getFocus() != null ? this.getFocus().getY() : 0);
  }

  @Override
  public Point2D getViewPortDimensionCenter(final IEntity entity) {
    final Point2D viewPortLocation = this.getViewPortLocation(entity);
    if (entity.getAnimationController() == null || entity.getAnimationController().getCurrentAnimation() == null) {
      return new Point2D.Double(viewPortLocation.getX() + entity.getWidth() * 0.5, viewPortLocation.getY() + entity.getHeight() * 0.5);
    }

    final Spritesheet spriteSheet = entity.getAnimationController().getCurrentAnimation().getSpritesheet();
    return new Point2D.Double(viewPortLocation.getX() + spriteSheet.getSpriteWidth() * 0.5, viewPortLocation.getY() + spriteSheet.getSpriteHeight() * 0.5);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.gurkenlabs.liti.graphics.ICamera#getRenderLocation(de.gurkenlabs.liti.
   * entities.Entity)
   */
  @Override
  public Point2D getViewPortLocation(final IEntity entity) {
    // localplayer camera causes flickering and bouncing of the sprite
    if (entity.getAnimationController() != null && entity.getAnimationController().getCurrentAnimation() != null && entity.getAnimationController().getCurrentAnimation().getSpritesheet() != null) {
      final Spritesheet spriteSheet = entity.getAnimationController().getCurrentAnimation().getSpritesheet();
      final Point2D location = new Point2D.Double(entity.getLocation().getX() - (spriteSheet.getSpriteWidth() - entity.getWidth()) * 0.5, entity.getLocation().getY() - (spriteSheet.getSpriteHeight() - entity.getHeight()) * 0.5);
      return this.getViewPortLocation(location);
    }

    return this.getViewPortLocation(entity.getLocation());
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getRenderLocation(java.awt.geom.
   * Point2D)
   */
  @Override
  public Point2D getViewPortLocation(final Point2D mapLocation) {
    return this.getViewPortLocation(mapLocation.getX(), mapLocation.getY());
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getRenderLocation(double, double)
   */
  @Override
  public Point2D getViewPortLocation(final double x, final double y) {
    return new Point2D.Double(x + this.getPixelOffsetX(), y + this.getPixelOffsetY());
  }

  @Override
  public void setFocus(final Point2D focus) {
    // dunno why but without the factor of 0.01 sometimes everything starts to get wavy while rendering ...
    // it seems to be an issue with the focus location being exactly dividable by up to 4?? (maybe even more for higher renderscales)
    // this is somehow related to the rendering scale: if the rendering scale is lower this will only be affected by lower dividable numbers (e.g. renderscale of 6 only has an issue with 1 and 0.5)
    // seems like java cannot place certain images onto their exact pixel location with an AffineTransform...
    double fraction = focus.getY() - Math.floor(focus.getY());
    if(MathUtilities.isInt(fraction * 4)){
      focus.setLocation(focus.getX(), focus.getY() + 0.01);
    }
    
    this.focus = focus;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#shake(int, int)
   */
  @Override
  public void shake(final double intensity, final int delay, final int shakeDuration) {
    this.shakeTick = Game.getLoop().getTicks();
    this.shakeDelay = delay;
    this.shakeIntensity = intensity;
    this.shakeDuration = shakeDuration;
  }

  @Override
  public void setZoom(float zoom, int delay) {
    if (delay == 0) {
      Game.getInfo().setRenderScale(zoom);
      for (Consumer<Float> cons : this.zoomChangedConsumer) {
        cons.accept(zoom);
      }
      
      this.zoom = 0;
      this.zoomDelay = 0;
      this.zoomTick = 0;
      this.zoomStep = 0;
    } else {
      this.zoomTick = Game.getLoop().getTicks();
      this.zoom = zoom;
      this.zoomDelay = delay;

      double tickduration = 1000 / Game.getLoop().getUpdateRate();
      double tickAmount = delay / tickduration;
      float totalDelta = zoom - Game.getInfo().getRenderScale();
      this.zoomStep = tickAmount > 0 ? (float) (totalDelta / tickAmount) : totalDelta;
    }
  }

  @Override
  public float getZoom() {
    return this.zoom;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.ICamera#getCameraRegion()
   */
  @Override
  public Rectangle2D getViewPort() {
    return this.viewPort;
  }

  @Override
  public void updateFocus() {
    this.setFocus(this.applyShakeEffect(this.getFocus()));
    double viewPortY = this.getFocus().getY() - this.getViewPortCenterY();
    this.viewPort = new Rectangle2D.Double(this.getFocus().getX() - this.getViewPortCenterX(), viewPortY, Game.getScreenManager().getResolution().getWidth() / Game.getInfo().getRenderScale(), Game.getScreenManager().getResolution().getHeight() / Game.getInfo().getRenderScale());
  }

  @Override
  public void onZoomChanged(Consumer<Float> zoomCons) {
    this.zoomChangedConsumer.add(zoomCons);
  }

  /**
   * Gets the shake duration.
   *
   * @return the shake duration
   */
  private int getShakeDuration() {
    return this.shakeDuration;
  }

  /**
   * Gets the shake offset.
   *
   * @return the shake offset
   */
  private double getShakeIntensity() {
    return this.shakeIntensity;
  }

  /**
   * Gets the shake tick.
   *
   * @return the shake tick
   */
  private long getShakeTick() {
    return this.shakeTick;
  }

  /**
   * Apply shake effect.
   *
   * @param cameraLocation
   *          the camera location
   * @return the point2 d
   */
  private Point2D applyShakeEffect(final Point2D cameraLocation) {
    if (this.isShakeEffectActive()) {
      return new Point2D.Double(cameraLocation.getX() + this.shakeOffsetX, cameraLocation.getY() + this.shakeOffsetY);
    }

    return cameraLocation;
  }

  private boolean isShakeEffectActive() {
    return this.getShakeTick() != 0 && Game.getLoop().getDeltaTime(this.getShakeTick()) < this.getShakeDuration();
  }

  private double getViewPortCenterX() {
    return Game.getScreenManager().getResolution().getWidth() * 0.5 / Game.getInfo().getRenderScale();
  }

  private double getViewPortCenterY() {
    return Game.getScreenManager().getResolution().getHeight() * 0.5 / Game.getInfo().getRenderScale();
  }
}