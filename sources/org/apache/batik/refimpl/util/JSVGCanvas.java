/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.refimpl.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.NoninvertibleTransformException;

import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;

import org.apache.batik.gvt.GraphicsNode;

import org.apache.batik.gvt.event.AbstractEventDispatcher;

import org.apache.batik.gvt.renderer.Renderer;
import org.apache.batik.gvt.renderer.RendererFactory;

import org.apache.batik.refimpl.bridge.ConcreteGVTBuilder;
import org.apache.batik.refimpl.bridge.DefaultUserAgent;
import org.apache.batik.refimpl.bridge.SVGBridgeContext;

import org.apache.batik.refimpl.gvt.ConcreteGVTFactory;

import org.apache.batik.refimpl.gvt.filter.ConcreteGraphicsNodeRableFactory;
import org.apache.batik.refimpl.gvt.renderer.StaticRenderer;
import org.apache.batik.refimpl.gvt.renderer.StaticRendererFactory;

import org.apache.batik.refimpl.parser.ParserFactory;

import org.apache.batik.refimpl.script.ConcreteInterpreterPool;

import org.apache.batik.util.SVGUtilities;

import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.MissingListenerException;

import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;


/**
 * This class represents a JComponent which is able to represents
 * a SVG document.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @author <a href="mailto:cjolif@ilog.fr">Christophe Jolif</a>
 * @version $Id$
 */
public class JSVGCanvas
    extends    JComponent
    implements ActionMap {
    // The actions names.
    public final static String UNZOOM_ACTION = "UnzoomAction";
    public final static String ZOOM_IN_ACTION = "ZoomInAction";
    public final static String ZOOM_OUT_ACTION = "ZoomOutAction";

    /**
     * The cursor for panning.
     */
    public final static Cursor PAN_CURSOR =
        new Cursor(Cursor.MOVE_CURSOR);

    /**
     * The cursor for zooming.
     */
    public final static Cursor ZOOM_CURSOR =
        new Cursor(Cursor.DEFAULT_CURSOR);

    /**
     * The cursor for rotating.
     */
    public final static Cursor ROTATE_CURSOR =
        new Cursor(Cursor.DEFAULT_CURSOR);

    /**
     * The global offscreen buffer.
     */
    protected BufferedImage globalBuffer;

    /**
     * The current offscreen buffer.
     */
    protected BufferedImage buffer;

    /**
     * Root of the GVT tree displayed by this viewer
     */
    protected GraphicsNode gvtRoot;

    /**
     * The current renderer.
     */
    protected Renderer renderer;

    /**
     * The renderer factory.
     */
    protected RendererFactory rendererFactory;

    /**
     * The GVT builder.
     */
    protected GVTBuilder builder;

    /**
     * The bridge context.
     */
    protected BridgeContext bridgeContext;

    /**
     * The SVG document to render.
     */
    protected SVGDocument document;

    /**
     * Must the buffer be updated?
     */
    protected boolean repaint;

    /**
     * The user agent.
     */
    protected UserAgent userAgent;

    /**
     * The tranform to apply to the graphics object.
     */
    protected AffineTransform transform = new AffineTransform();

    /**
     * The tranform representing the pan tranlate.
     */
    protected AffineTransform panTransform;

    /**
     * The tranform representing the rotation.
     */
    protected AffineTransform rotateTransform;

    /**
     * The zoom marker top line.
     */
    protected Line2D markerTop;

    /**
     * The zoom marker left line.
     */
    protected Line2D markerLeft;

    /**
     * The zoom marker bottom line.
     */
    protected Line2D markerBottom;

    /**
     * The zoom marker right line.
     */
    protected Line2D markerRight;

    /**
     * The rotate marker.
     */
    protected Shape rotateMarker;

    /**
     * The repaint thread.
     */
    protected Thread repaintThread;

    /**
     * The thumbnail canvas.
     */
    protected ThumbnailCanvas thumbnailCanvas;

    /**
     * Used to draw marker
     */
    protected BasicStroke markerStroke
        = new BasicStroke(1, BasicStroke.CAP_SQUARE,
                          BasicStroke.JOIN_MITER,
                          10,
                          new float[]{4, 4}, 0);
    /**
     * Creates a new SVG canvas.
     */
    public JSVGCanvas() {
        this(new DefaultUserAgent());
    }

    /**
     * Creates a new SVG canvas.
     */
    public JSVGCanvas(UserAgent ua) {
        userAgent = ua;

        // for event dispatching inside GVT
        AbstractEventDispatcher dispatcher =
            (AbstractEventDispatcher)userAgent.getEventDispatcher();
        if (dispatcher != null) {
            addMouseListener(dispatcher);
            addMouseMotionListener(dispatcher);
            addKeyListener(dispatcher);
        }

        rendererFactory = new StaticRendererFactory();

        builder = new ConcreteGVTBuilder();
        bridgeContext = new SVGBridgeContext();
        bridgeContext.setGVTFactory
            (ConcreteGVTFactory.getGVTFactoryImplementation());
        bridgeContext.setParserFactory(new ParserFactory());
        bridgeContext.setUserAgent(userAgent);
        bridgeContext.setGraphicsNodeRableFactory
            (new ConcreteGraphicsNodeRableFactory());
        ((SVGBridgeContext)bridgeContext).setInterpreterPool
            (new ConcreteInterpreterPool());

        addComponentListener(new CanvasListener());
        MouseListener ml = new MouseListener();
        addMouseListener(ml);
        addMouseMotionListener(ml);

        listeners.put(UNZOOM_ACTION, new UnzoomAction());
        listeners.put(ZOOM_IN_ACTION, new ZoomInAction());
        listeners.put(ZOOM_OUT_ACTION, new ZoomOutAction());
    }

    /**
     * Sets the renderer factory to use to create the renderer.
     */
    public void setRendererFactory(RendererFactory rf) {
        rendererFactory = rf;
        repaint();
    }

    /**
     * Returns the current renderer factory.
     */
    public RendererFactory getRendererFactory() {
        return rendererFactory;
    }

    /**
     * Sets the SVG document to display.
     * @param doc if null, clears the canvas.
     */
    public void setSVGDocument(SVGDocument doc) {
        if (document != null) {
            // fire the unload event
            Event evt = document.createEvent("SVGEvents");
            evt.initEvent("SVGUnload", false, false);
            ((EventTarget)(document.
                           getRootElement())).
                dispatchEvent(evt);
        }
        document = doc;
        if (document == null) {
            gvtRoot = null;
        } else {
            bridgeContext.setViewCSS((ViewCSS)doc.getDocumentElement());
            gvtRoot = builder.build(bridgeContext, document);
            computeTransform();
        }

        if (userAgent.getEventDispatcher() != null)
            userAgent.getEventDispatcher().setRootNode(gvtRoot);

        repaint = true;
        repaint();

        if (thumbnailCanvas != null) {
            thumbnailCanvas.fullRepaint();
        }
    }

    /**
     * Returns the thumbnail.
     */
    public JComponent getThumbnail() {
        if (thumbnailCanvas == null) {
            thumbnailCanvas = new ThumbnailCanvas();
        }
        return thumbnailCanvas;
    }

    // ActionMap /////////////////////////////////////////////////////

    /**
     * The map that contains the listeners
     */
    protected Map listeners = new HashMap();

    /**
     * Returns the action associated with the given string
     * or null on error
     * @param key the key mapped with the action to get
     * @throws MissingListenerException if the action is not found
     */
    public Action getAction(String key) throws MissingListenerException {
        return (Action)listeners.get(key);
    }

    /**
     * Clears the offscreen buffer.
     */
    protected void clearBuffer(int w, int h) {
        Graphics2D g = buffer.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.setClip(0, 0, w, h);
        g.setPaint(Color.white);
        g.fillRect(0, 0, w, h);
    }

   /**
    * @return the area of interest displayed in the viewer, in usr space.
    */
    protected Shape getAreaOfInterest(Rectangle devAOI){
        AffineTransform dev2usr = null;
        try {
            dev2usr = transform.createInverse();
        } catch(NoninvertibleTransformException e){
            // This should not happen. See setTransform
            throw new Error();
        }

        System.out.println("devAOI : " + devAOI);
        System.out.println("usrAOI : " + dev2usr.createTransformedShape(devAOI).getBounds2D());
        System.out.println("devAOI 2: " + transform.createTransformedShape(dev2usr.createTransformedShape(devAOI)).getBounds2D());
        return dev2usr.createTransformedShape(devAOI);
    }


    /**
     * Paints this component.
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Dimension size = getSize();
        int w = size.width;
        int h = size.height;

        if (w < 1 || h < 1) {
            return;
        }

        if (repaintThread != null) {
            Graphics2D g2d = (Graphics2D)g;
            g2d.drawImage(buffer, null, 0, 0);
            return;
        }

        updateBuffer(w, h);
        if (repaint) {
            renderer = rendererFactory.createRenderer(buffer);
            renderer.setTransform(transform);
        }
        if (renderer != null && gvtRoot != null &&
            renderer.getTree() != gvtRoot) {
            renderer.setTree(gvtRoot);
            repaint = true;
        }
        if (repaint) {
            Graphics2D g2d = (Graphics2D)g;
            if (panTransform != null) {
                int tx = (int)panTransform.getTranslateX();
                int ty = (int)panTransform.getTranslateY();
                paintPanRegions(g2d, tx, ty, w, h);
                g2d.transform(panTransform);
                panTransform = null;
                g2d.drawImage(buffer, null, 0, 0);
            } else {
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setClip(0, 0, w, h);
                g2d.setPaint(Color.white);
                g2d.fillRect(0, 0, w, h);
            }

            clearBuffer(w, h);
            renderer.setTransform(transform);

            repaintThread = new RepaintThread();
            repaintThread.start();
            repaint = false;
            return;
        }
        repaint = false;
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        if (panTransform != null) {
            int tx = (int)panTransform.getTranslateX();
            int ty = (int)panTransform.getTranslateY();
            paintPanRegions(g2d, tx, ty, w, h);
            g2d.transform(panTransform);
        }
        g2d.drawImage(buffer, null, 0, 0);
        g2d.setXORMode(Color.white);
        if (markerTop != null) {
            g2d.setColor(Color.black);
            g2d.setStroke(markerStroke);
            g2d.draw(markerTop);
            g2d.draw(markerLeft);
            g2d.draw(markerBottom);
            g2d.draw(markerRight);
        } else if (rotateMarker != null) {
            g2d.setColor(Color.black);
            g2d.setStroke(markerStroke);
            g2d.draw(rotateMarker);
        }
        g2d.setXORMode(Color.white);
    }

    /**
     * Repaints the pan blank regions.
     */
    protected void paintPanRegions(Graphics2D g2d, int tx, int ty,
                                   int w, int h) {
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setPaint(Color.white);
        if (tx > 0) {
            if (ty > 0) {
                g2d.fillRect(0, 0, w, ty);
                g2d.fillRect(0, ty, tx, h - ty);
            } else {
                g2d.fillRect(0, 0, tx, h);
                g2d.fillRect(tx, h + ty, w - tx, -ty);
            }
        } else {
            if (ty > 0) {
                g2d.fillRect(0, 0, w, ty);
                g2d.fillRect(w + tx, ty, -tx, h - ty);
            } else {
                g2d.fillRect(0, h + ty, w, -ty);
                g2d.fillRect(w + tx, 0, -tx, h + ty);
            }
        }
    }

    /**
     * Updates the offscreen buffer.
     * @param w&nbsp;h The size of the component.
     */
    protected void updateBuffer(int w, int h) {
        // Create a new buffer if needed.
        if (globalBuffer == null ||
            globalBuffer.getWidth() < w ||
            globalBuffer.getHeight() < h) {
            globalBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            buffer = globalBuffer;
            repaint = true;
        } else if (buffer.getWidth() != w ||
                   buffer.getHeight() != h) {
            buffer = globalBuffer.getSubimage(0, 0, w, h);
            repaint = true;
        }
    }

    /**
     * Computes the value of the transform attribute.
     */
    protected void computeTransform() {
        SVGSVGElement elt = document.getRootElement();
        Dimension size = getSize();
        int w = size.width;
        int h = size.height;

        transform = SVGUtilities.getPreserveAspectRatioTransform
            (elt, w, h, bridgeContext.getParserFactory());
    }

    /**
     * To repaint the buffer.
     */
    protected class RepaintThread extends Thread {
        /**
         * Creates a new thread.
         */
        public RepaintThread() {
            setPriority(Thread.MIN_PRIORITY);
        }

        /**
         * The thread main method.
         */
        public void run() {
            Dimension size = getSize();
            renderer.repaint(getAreaOfInterest
                             (new Rectangle(0, 0, size.width, size.height)));
            repaintThread = null;
            repaint();
        }
    }

    /**
     * To correctly resize the view.
     */
    protected class CanvasListener extends ComponentAdapter {
        public CanvasListener() {}
        public void componentResized(ComponentEvent e) {
            if (gvtRoot == null) {
                return;
            }
            computeTransform();
            if (thumbnailCanvas != null) {
                thumbnailCanvas.repaint();
            }
        }
    }

    /**
     * To reset the zoom.
     */
    public class UnzoomAction extends AbstractAction {
        public UnzoomAction() {}
        public void actionPerformed(ActionEvent e) {
            computeTransform();
            repaint = true;
            repaint();
            if (thumbnailCanvas != null) {
                thumbnailCanvas.repaint();
            }
        }
    }

    /**
     * To zoom in the document.
     */
    public class ZoomInAction extends AbstractAction {
        public ZoomInAction() {}
        public void actionPerformed(ActionEvent e) {
            transform.preConcatenate
                (AffineTransform.getScaleInstance(2, 2));
            repaint = true;
            repaint();
            if (thumbnailCanvas != null) {
                thumbnailCanvas.repaint();
            }
        }
    }

    /**
     * To zoom out the document.
     */
    public class ZoomOutAction extends AbstractAction {
        public ZoomOutAction() {}
        public void actionPerformed(ActionEvent e) {
            transform.preConcatenate
                (AffineTransform.getScaleInstance(0.5, 0.5));
            repaint = true;
            repaint();
            if (thumbnailCanvas != null) {
                thumbnailCanvas.repaint();
            }
        }
    }

    /**
     * To handle the mouse events.
     */
    protected class MouseListener
        extends    MouseAdapter
        implements MouseMotionListener {

        protected Cursor cursor;
        protected int sx;
        protected int sy;
        protected boolean mouseExited;

        public MouseListener() {}
        public void mousePressed(MouseEvent e) {
            int mods = e.getModifiers();

            if ((mods & e.BUTTON1_MASK) != 0) {
                mouseExited = false;
                sx = e.getX();
                sy = e.getY();
                if ((mods & e.SHIFT_MASK) != 0) {
                    if (cursor == null) {
                        cursor = getCursor();
                    }
                    setCursor(PAN_CURSOR);
                    panTransform = new AffineTransform();
                } else if ((mods & e.CTRL_MASK) != 0) {
                    if (cursor == null) {
                        cursor = getCursor();
                    }
                    setCursor(ZOOM_CURSOR);
                }
            } else if ((mods & e.BUTTON3_MASK) != 0) {
                mouseExited = false;
                sx = e.getX();
                sy = e.getY();
                if ((mods & e.CTRL_MASK) != 0) {
                    if (cursor == null) {
                        cursor = getCursor();
                    }
                    setCursor(ROTATE_CURSOR);
                    rotateTransform = new AffineTransform();
                    paintRotateMarker(sx, sy);
                }
            }
        }
        public void mouseDragged(MouseEvent e) {
            int mods = e.getModifiers();

            if ((mods & e.BUTTON1_MASK) != 0) {
                if ((mods & e.SHIFT_MASK) != 0 && panTransform != null) {
                    if (!mouseExited) {
                        int x = e.getX();
                        int y = e.getY();
                        panTransform.translate(x - sx, y - sy);
                        sx = x;
                        sy = y;
                        repaint();
                    }
                } else if ((mods & e.CTRL_MASK) != 0) {
                    paintZoomMarker(e.getX(), e.getY());
                } else {
                    endOperation(e.getX(), e.getY());
                }
            } else if ((mods & e.BUTTON3_MASK) != 0) {
                if ((mods & e.CTRL_MASK) != 0) {
                    paintRotateMarker(e.getX(), e.getY());
                }
            }

        }
        public void mouseReleased(MouseEvent e) {
            int mods = e.getModifiers();

            if ((mods & e.BUTTON1_MASK) != 0) {
                if (cursor != null) {
                    endOperation(e.getX(), e.getY());
                }
            } else if ((mods & e.BUTTON3_MASK) != 0) {
                if (cursor != null) {
                    endOperation(e.getX(), e.getY());
                }
            }

        }
        public void mouseMoved(MouseEvent e) {
        }
        public void mouseExited(MouseEvent e) {
            mouseExited = true;
            markerTop = null;
            markerLeft = null;
            markerBottom = null;
            markerRight = null;
            panTransform = null;
            repaint();
        }
        protected void endOperation(int x, int y) {
            setCursor(cursor);
            cursor = null;

            if (mouseExited) {
                repaint();
                return;
            }

            if (panTransform != null) {
                panTransform.translate(x - sx, y - sy);
                transform.preConcatenate(panTransform);
                repaint = true;
                repaint();
                if (thumbnailCanvas != null) {
                    thumbnailCanvas.repaint();
                }
            } else if (markerTop != null) {
                clearZoomMarker();
                Dimension size = getSize();

                if (x < sx) {
                    int tmp = x;
                    x = sx;
                    sx = tmp;
                }

                if (y < sy) {
                    int tmp = y;
                    y = sy;
                    sy = tmp;
                }

                // Process zoom factor
                float scaleX = size.width / (float)(x - sx);
                float scaleY = size.height / (float)(y - sy);
                float scale = (scaleX < scaleY) ? scaleX : scaleY;

                // Process zoom transform
                AffineTransform at = new AffineTransform();
                at.scale(scale, scale);
                at.translate(-sx, -sy);

                transform.preConcatenate(at);
                repaint = true;
                repaint();
                if (thumbnailCanvas != null) {
                    thumbnailCanvas.repaint();
                }
            } else if (rotateMarker != null) {
                clearRotateMarker();
                transform.preConcatenate(rotateTransform);
                repaint = true;
                repaint();
                if (thumbnailCanvas != null) {
                    thumbnailCanvas.repaint();
                }
            }
        }
        protected void paintRotateMarker(int x, int y) {
            clearRotateMarker();

            if (mouseExited) {
                rotateMarker = null;
            } else {
                Dimension dim = getSize();
                int w = dim.width / 5;
                int h = dim.height / 5;

                GeneralPath p = new GeneralPath();
                p.moveTo(dim.width / 2 - w / 2, dim.height / 2 - h / 2);
                p.lineTo(dim.width / 2 - w / 2, dim.height / 2 + h / 2);
                p.lineTo(dim.width / 2 + w / 2, dim.height / 2 + h / 2);
                p.lineTo(dim.width / 2 + w / 2, dim.height / 2 - h / 2);
                p.closePath();
                p.moveTo(dim.width / 2, dim.height / 2 - h / 2);
                p.lineTo(dim.width / 2 + 8, dim.height / 2 - h / 2 + 8);
                p.moveTo(dim.width / 2, dim.height / 2 - h / 2);
                p.lineTo(dim.width / 2 - 8, dim.height / 2 - h / 2 + 8);

                double dx = x - dim.width / 2;
                double dy = y - dim.height / 2;
                double cos = -dy / Math.sqrt(dx * dx + dy * dy);

                rotateTransform =
                    AffineTransform.getRotateInstance
                    ((dx > 0) ? Math.acos(cos) : -Math.acos(cos),
                     dim.width / 2,
                     dim.height / 2);

                rotateMarker = rotateTransform.createTransformedShape(p);

                Rectangle r;
                r = markerStroke.createStrokedShape(rotateMarker).getBounds();
                paintImmediately(r.x, r.y, r.width, r.height);
            }
        }
        protected void clearRotateMarker() {
            if (rotateMarker != null) {
                Rectangle r;
                r = markerStroke.createStrokedShape(rotateMarker).getBounds();
                rotateMarker = null;
                paintImmediately(r.x, r.y, r.width, r.height);
            }
        }
        protected void paintZoomMarker(int x, int y) {
            clearZoomMarker();

            if (mouseExited) {
                markerTop = null;
                markerLeft = null;
                markerBottom = null;
                markerRight = null;
            } else {
                markerTop    = new Line2D.Float(sx, sy, x,  sy);
                markerLeft   = new Line2D.Float(sx, sy, sx, y);
                markerBottom = new Line2D.Float(sx, y,  x,  y);
                markerRight  = new Line2D.Float(x,  y,  x,  sy);

                Rectangle r;
                r = markerStroke.createStrokedShape(markerTop).getBounds();
                paintImmediately(r.x, r.y, r.width, r.height);

                r = markerStroke.createStrokedShape(markerLeft).getBounds();
                paintImmediately(r.x, r.y, r.width, r.height);

                r = markerStroke.createStrokedShape(markerBottom).getBounds();
                paintImmediately(r.x, r.y, r.width, r.height);

                r = markerStroke.createStrokedShape(markerRight).getBounds();
                paintImmediately(r.x, r.y, r.width, r.height);
            }
        }
        protected void clearZoomMarker() {
            if (markerTop != null) {
                Rectangle r;
                r = markerStroke.createStrokedShape(markerTop).getBounds();
                markerTop = null;
                paintImmediately(r.x, r.y, r.width, r.height);

                r = markerStroke.createStrokedShape(markerLeft).getBounds();
                markerLeft = null;
                paintImmediately(r.x, r.y, r.width, r.height);

                r = markerStroke.createStrokedShape(markerBottom).getBounds();
                markerBottom = null;
                paintImmediately(r.x, r.y, r.width, r.height);

                r = markerStroke.createStrokedShape(markerRight).getBounds();
                markerRight = null;
                paintImmediately(r.x, r.y, r.width, r.height);
            }
        }
    }

    /**
     * This class represents the thumbnail canvas.
     */
    protected class ThumbnailCanvas extends JComponent {
        /**
         * The current offscreen buffer.
         */
        protected BufferedImage offscreenBuffer;

        /**
         * The tranform to apply to the graphics object.
         */
        protected AffineTransform transform = new AffineTransform();

        /**
         * The current renderer.
         */
        protected Renderer renderer;

        /**
         * The repaint thread.
         */
        protected Thread repaintThread;

        /**
         * Must the buffer be updated?
         */
        protected boolean repaint;

        /**
         * Used to draw marker
         */
        protected BasicStroke markerStroke
            = new BasicStroke(1, BasicStroke.CAP_SQUARE,
                              BasicStroke.JOIN_MITER,
                              10,
                              new float[]{2, 2}, 0);
        /**
         * Creates a new ThumbnailCanvas object.
         */
        public ThumbnailCanvas() {
            addComponentListener(new ThumbnailCanvasListener());
        }

        /**
         * Recomputes the offscreen buffer and repaint.
         */
        public void fullRepaint() {
            repaint = true;
            computeTransform();
            repaint();
        }

        /**
         * Paints this component.
         */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Dimension size = getSize();
            int w = size.width;
            int h = size.height;

            if (w < 1 || h < 1) {
                return;
            }

            if (repaintThread != null) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.drawImage(offscreenBuffer, null, 0, 0);
                return;
            }

            updateBuffer(w, h);
            if (repaint) {
                renderer = rendererFactory.createRenderer(offscreenBuffer);
                renderer.setTransform(transform);
            }
            if (renderer != null && gvtRoot != null &&
                renderer.getTree() != gvtRoot) {
                renderer.setTree(gvtRoot);
                repaint = true;
            }
            if (repaint) {
                clearBuffer(w, h);
                renderer.setTransform(transform);

                repaintThread = new ThumbnailRepaintThread();
                repaintThread.start();
                repaint = false;
                return;
            }
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(offscreenBuffer, null, 0, 0);

            // Paint the marker
            Dimension csize = JSVGCanvas.this.getSize();
            Rectangle rect = new Rectangle(0, 0, csize.width, csize.height);

            Shape s = JSVGCanvas.this.getAreaOfInterest(rect);
            s = transform.createTransformedShape(s);
            g2d.setColor(Color.black);
            g2d.setStroke(markerStroke);
            g2d.setXORMode(Color.white);
            g2d.draw(s);
        }

        /**
         * Clears the offscreen buffer.
         */
        protected void clearBuffer(int w, int h) {
            Graphics2D g = offscreenBuffer.createGraphics();
            g.setComposite(AlphaComposite.SrcOver);
            g.setClip(0, 0, w, h);
            g.setPaint(Color.white);
            g.fillRect(0, 0, w, h);
        }

        /**
         * Updates the offscreen buffer.
         * @param w&nbsp;h The size of the component.
         */
        protected void updateBuffer(int w, int h) {
            // Create a new buffer if needed.
            if (offscreenBuffer == null ||
                offscreenBuffer.getWidth() < w ||
                offscreenBuffer.getHeight() < h) {
                offscreenBuffer =
                    new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                repaint = true;
            } else if (offscreenBuffer.getWidth() != w ||
                       offscreenBuffer.getHeight() != h) {
                offscreenBuffer = offscreenBuffer.getSubimage(0, 0, w, h);
                repaint = true;
            }
        }

        /**
         * Computes the value of the transform attribute.
         */
        protected void computeTransform() {
            if (document == null) {
                transform = new AffineTransform();
                return;
            }
            SVGSVGElement elt = document.getRootElement();
            Dimension size = getSize();
            int w = size.width;
            int h = size.height;

            transform = SVGUtilities.getPreserveAspectRatioTransform
                (elt, w, h, bridgeContext.getParserFactory());
            if (transform.isIdentity()) {
                float dw = elt.getWidth().getBaseVal().getValue();
                float dh = elt.getHeight().getBaseVal().getValue();

                transform = AffineTransform.getScaleInstance(w / (float)dw,
                                                             h / (float)dh);
            }
        }

        /**
         * @return the area of interest displayed in the viewer, in usr space.
         */
        protected Shape getAreaOfInterest(Rectangle devAOI){
            AffineTransform dev2usr = null;
            try {
                dev2usr = transform.createInverse();
            } catch(NoninvertibleTransformException e){
                // This should not happen. See setTransform
                throw new Error();
            }
            return dev2usr.createTransformedShape(devAOI);
        }

        /**
         * To repaint the buffer.
         */
        protected class ThumbnailRepaintThread extends Thread {
            /**
             * Creates a new thread.
             */
            public ThumbnailRepaintThread() {
                setPriority(Thread.MIN_PRIORITY);
            }

            /**
             * The thread main method.
             */
            public void run() {
                Dimension size = getSize();
                renderer.repaint(getAreaOfInterest
                               (new Rectangle(0, 0, size.width, size.height)));
                repaintThread = null;
                repaint();
            }
        }

        /**
         * To correctly resize the view.
         */
        protected class ThumbnailCanvasListener extends ComponentAdapter {
            public ThumbnailCanvasListener() {}
            public void componentResized(ComponentEvent e) {
                if (gvtRoot == null) {
                    return;
                }
                computeTransform();
            }
        }

    }

}
