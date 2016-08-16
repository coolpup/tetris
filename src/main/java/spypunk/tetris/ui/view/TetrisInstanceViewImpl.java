/*
 * Copyright © 2016 spypunk <spypunk@gmail.com>
 *
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package spypunk.tetris.ui.view;

import static spypunk.tetris.ui.constants.TetrisUIConstants.BLOCK_SIZE;
import static spypunk.tetris.ui.constants.TetrisUIConstants.DEFAULT_BORDER_COLOR;
import static spypunk.tetris.ui.constants.TetrisUIConstants.DEFAULT_FONT_COLOR;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import spypunk.tetris.constants.TetrisConstants;
import spypunk.tetris.model.Block;
import spypunk.tetris.model.Shape;
import spypunk.tetris.model.ShapeType;
import spypunk.tetris.model.Tetris;
import spypunk.tetris.model.TetrisInstance;
import spypunk.tetris.model.TetrisInstance.State;
import spypunk.tetris.ui.cache.ImageCache;
import spypunk.tetris.ui.factory.FontFactory;
import spypunk.tetris.ui.util.SwingUtils;

public class TetrisInstanceViewImpl extends TetrisInstanceView {

    private static final long serialVersionUID = -3487901883637598196L;

    private static final String PAUSE = "PAUSE";

    private static final float TETRIS_FROZEN_FONT_SIZE = 42F;

    private static final Color TETRIS_FROZEN_FG_COLOR = new Color(30, 30, 30, 200);

    private static final String GAME_OVER = "GAME OVER";

    private final BufferedImage image;

    private final Tetris tetris;

    private final Font frozenTetrisFont;

    private final TetrisInstanceStatisticsView tetrisInstanceStatisticsView;

    private final TetrisInstanceInfoView tetrisInstanceInfoView;

    private final Rectangle gridRectangle;

    private final Rectangle frozenGridRectangle;

    private final ImageCache imageCache;

    private final int blockX;

    private final int blockY;

    public TetrisInstanceViewImpl(FontFactory fontFactory,
            TetrisInstanceStatisticsView tetrisInstanceStatisticsView,
            TetrisInstanceInfoView tetrisInstanceInfoView,
            ImageCache imageCache,
            Tetris tetris) {
        this.tetrisInstanceStatisticsView = tetrisInstanceStatisticsView;
        this.tetrisInstanceInfoView = tetrisInstanceInfoView;
        this.imageCache = imageCache;
        this.tetris = tetris;

        frozenTetrisFont = fontFactory.createDefaultFont(TETRIS_FROZEN_FONT_SIZE);

        gridRectangle = new Rectangle(0, 0, TetrisConstants.WIDTH * BLOCK_SIZE + 1,
                TetrisConstants.HEIGHT * BLOCK_SIZE + 1);

        frozenGridRectangle = new Rectangle(gridRectangle.x + 1, gridRectangle.y + 1, gridRectangle.width - 1,
                gridRectangle.height - 1);

        blockX = gridRectangle.x + 1;
        blockY = gridRectangle.y + 1;

        image = new BufferedImage(gridRectangle.width + 1, gridRectangle.height + 1,
                BufferedImage.TYPE_INT_ARGB);

        final JLabel label = new JLabel(new ImageIcon(image));

        setLayout(new BorderLayout(BLOCK_SIZE, 0));
        setBackground(Color.BLACK);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE));
        setIgnoreRepaint(true);

        add(label, BorderLayout.CENTER);
        add(tetrisInstanceStatisticsView, BorderLayout.WEST);
        add(tetrisInstanceInfoView, BorderLayout.EAST);
    }

    @Override
    public void update() {
        if (tetris.getTetrisInstance() == null) {
            return;
        }

        SwingUtils.doInGraphics(image, this::renderBlocks);

        tetrisInstanceStatisticsView.update();
        tetrisInstanceInfoView.update();

        repaint();
    }

    private void renderBlocks(final Graphics2D graphics) {
        final TetrisInstance tetrisInstance = tetris.getTetrisInstance();

        graphics.setColor(DEFAULT_BORDER_COLOR);

        graphics.drawRect(gridRectangle.x, gridRectangle.y, gridRectangle.width,
            gridRectangle.height);

        tetrisInstance.getBlocks().values().stream()
                .forEach(block -> renderBlock(graphics, block));

        final Shape currentShape = tetrisInstance.getCurrentShape();

        if (currentShape != null) {
            currentShape.getBlocks().stream().forEach(block -> renderBlock(graphics, block));
        }

        final State state = tetrisInstance.getState();

        if (!State.RUNNING.equals(state)) {
            renderTetrisFrozen(graphics, state);
        }
    }

    private void renderBlock(Graphics2D graphics, Block block) {
        final ShapeType shapeType = block.getShape().getShapeType();
        final Image blockImage = imageCache.getBlockImage(shapeType);
        final Point location = block.getLocation();
        final int x1 = blockX + location.x * BLOCK_SIZE;
        final int y1 = blockY + location.y * BLOCK_SIZE;
        final Rectangle rectangle = new Rectangle(x1, y1, BLOCK_SIZE, BLOCK_SIZE);

        SwingUtils.drawImage(graphics, blockImage, rectangle);
    }

    private void renderTetrisFrozen(Graphics2D graphics, State state) {
        graphics.setColor(TETRIS_FROZEN_FG_COLOR);
        graphics.fillRect(frozenGridRectangle.x, frozenGridRectangle.y, frozenGridRectangle.width,
            frozenGridRectangle.height);

        SwingUtils.renderCenteredText(graphics, State.GAME_OVER.equals(state) ? GAME_OVER : PAUSE, gridRectangle,
            frozenTetrisFont, DEFAULT_FONT_COLOR);
    }
}
