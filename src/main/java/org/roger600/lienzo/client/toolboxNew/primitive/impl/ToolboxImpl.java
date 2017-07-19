package org.roger600.lienzo.client.toolboxNew.primitive.impl;

import java.util.Iterator;

import com.ait.lienzo.client.core.event.NodeMouseEnterHandler;
import com.ait.lienzo.client.core.event.NodeMouseExitHandler;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.Direction;
import com.google.gwt.core.client.GWT;
import org.roger600.lienzo.client.toolboxNew.Positions;
import org.roger600.lienzo.client.toolboxNew.grid.Point2DGrid;
import org.roger600.lienzo.client.toolboxNew.primitive.AbstractDefaultItem;
import org.roger600.lienzo.client.toolboxNew.primitive.DecoratorItem;
import org.roger600.lienzo.client.toolboxNew.primitive.DefaultItem;
import org.roger600.lienzo.client.toolboxNew.primitive.DefaultToolbox;
import org.roger600.lienzo.client.toolboxNew.util.Supplier;

public class ToolboxImpl
        implements DefaultToolbox<ToolboxImpl> {

    private final AbstractDefaultItem groupPrimitiveItem;
    private Supplier<BoundingBox> boundingBoxSupplier;
    private Direction at;
    private Point2D offset;
    private Runnable refreshCallback;
    private final ItemsImpl items;

    public ToolboxImpl(final Supplier<BoundingBox> boundingBoxSupplier) {
        this(boundingBoxSupplier,
             new GroupItem());
    }

    ToolboxImpl(final Supplier<BoundingBox> boundingBoxSupplier,
                final AbstractDefaultItem groupPrimitiveItem) {
        this.boundingBoxSupplier = boundingBoxSupplier;
        this.groupPrimitiveItem = groupPrimitiveItem;
        this.at = Direction.NORTH_EAST;
        this.offset = new Point2D(0d,
                                  0d);
        this.refreshCallback = new Runnable() {
            @Override
            public void run() {
                groupPrimitiveItem.getPrimitive().getLayer().batch();
            }
        };
        this.items = new ItemsImpl(groupPrimitiveItem);
    }

    @Override
    public ToolboxImpl attachTo(final Layer layer) {
        layer.add(groupPrimitiveItem.asPrimitive());
        return this;
    }

    @Override
    public ToolboxImpl at(final Direction at) {
        this.at = at;
        return checkReposition();
    }

    @Override
    public Point2DGrid getGrid() {
        return items.getGrid();
    }

    @Override
    public ToolboxImpl offset(final Point2D offset) {
        this.offset = offset;
        return checkReposition();
    }

    @Override
    public ToolboxImpl grid(final Point2DGrid grid) {
        items.grid(grid);
        return this;
    }

    @Override
    public ToolboxImpl add(final DefaultItem... items) {
        this.items.add(items);
        return this;
    }

    @Override
    public Iterator<DefaultItem> iterator() {
        return items.iterator();
    }

    @Override
    public ToolboxImpl decorate(final DecoratorItem<?> decorator) {
        groupPrimitiveItem.decorate(decorator);
        return this;
    }

    @Override
    public ToolboxImpl show() {
        groupPrimitiveItem.show();
        // TODO: Only do if show is executed (callback)
        reposition();
        items.show();
        return this;
    }

    public ToolboxImpl refresh() {
        checkReposition();
        items.refresh();
        return this;
    }

    @Override
    public boolean isVisible() {
        return groupPrimitiveItem.isVisible();
    }

    @Override
    public ToolboxImpl onMouseEnter(final NodeMouseEnterHandler handler) {
        groupPrimitiveItem.onMouseEnter(handler);
        return this;
    }

    @Override
    public ToolboxImpl onMouseExit(final NodeMouseExitHandler handler) {
        groupPrimitiveItem.onMouseExit(handler);
        return this;
    }

    @Override
    public ToolboxImpl hide() {
        groupPrimitiveItem.hide();
        // TODO: Only do if show is executed (callback)
        fireRefresh();
        return this;
    }

    @Override
    public void destroy() {
        items.destroy();
        groupPrimitiveItem.destroy();
        at = null;
        refreshCallback = null;
    }

    ItemsImpl getItems() {
        return items;
    }

    private ToolboxImpl checkReposition() {
        if (isVisible()) {
            reposition();
        }
        return this;
    }

    private void reposition() {
        GWT.log("BB is = " + boundingBoxSupplier.get());
        Point2D loc = Positions.anchorFor(boundingBoxSupplier.get(),
                                          this.at);
        GWT.log("OFFSET = " + offset);
        GWT.log("LOC = " + loc);
        groupPrimitiveItem.asPrimitive().setLocation(loc.offset(offset));
        fireRefresh();
    }

    private void fireRefresh() {
        if (null != refreshCallback) {
            refreshCallback.run();
        }
    }
}
