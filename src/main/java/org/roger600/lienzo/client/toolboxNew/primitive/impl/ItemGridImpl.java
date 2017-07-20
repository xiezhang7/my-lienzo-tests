package org.roger600.lienzo.client.toolboxNew.primitive.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.ait.lienzo.client.core.event.NodeMouseEnterHandler;
import com.ait.lienzo.client.core.event.NodeMouseExitHandler;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.types.Point2D;
import com.google.gwt.core.client.GWT;
import org.roger600.lienzo.client.toolboxNew.ItemGrid;
import org.roger600.lienzo.client.toolboxNew.grid.Point2DGrid;
import org.roger600.lienzo.client.toolboxNew.primitive.AbstractPrimitiveItem;
import org.roger600.lienzo.client.toolboxNew.primitive.DecoratedItem;
import org.roger600.lienzo.client.toolboxNew.primitive.DecoratorItem;

public class ItemGridImpl
        extends AbstractPrimitiveItem<ItemGridImpl>
        implements ItemGrid<ItemGridImpl, Point2DGrid, DecoratedItem>,
                   DecoratedItem<ItemGridImpl> {

    private final AbstractGroupItem groupPrimitiveItem;
    private final List<AbstractPrimitiveItem> items = new LinkedList<>();
    private Point2DGrid grid;
    private Runnable refreshCallback;

    public ItemGridImpl() {
        this(new GroupItem());
    }

    ItemGridImpl(final AbstractGroupItem groupPrimitiveItem) {
        this.groupPrimitiveItem = groupPrimitiveItem;
        this.refreshCallback = new Runnable() {
            @Override
            public void run() {
                if (null != groupPrimitiveItem.asPrimitive().getLayer()) {
                    groupPrimitiveItem.asPrimitive().getLayer().batch();
                }
            }
        };
    }

    @Override
    public ItemGridImpl grid(final Point2DGrid grid) {
        this.grid = grid;
        return checkReposition();
    }

    @Override
    public ItemGridImpl add(final DecoratedItem... items) {
        for (final DecoratedItem item : items) {
            try {
                final AbstractPrimitiveItem button = (AbstractPrimitiveItem) item;
                this.items.add(button);
                if (isVisible()) {
                    button.show();
                } else {
                    button.hide();
                }
                groupPrimitiveItem.getGroupItem().add(button.asPrimitive());
            } catch (final ClassCastException e) {
                throw new UnsupportedOperationException("This item only supports subtypes " +
                                                                "of " + AbstractPrimitiveItem.class.getName());
            }
        }
        return checkReposition();
    }

    @Override
    public Iterator<DecoratedItem> iterator() {
        return new ListItemsIterator<AbstractPrimitiveItem>(items) {
            @Override
            protected void remove(final AbstractPrimitiveItem item) {
                items.remove(item);
                checkReposition();
            }
        };
    }

    @Override
    public boolean isVisible() {
        return groupPrimitiveItem.isVisible();
    }

    @Override
    public ItemGridImpl decorate(final DecoratorItem<?> decorator) {
        groupPrimitiveItem.decorate(decorator);
        return this;
    }

    @Override
    public ItemGridImpl onMouseEnter(final NodeMouseEnterHandler handler) {
        groupPrimitiveItem.onMouseEnter(handler);
        return this;
    }

    @Override
    public ItemGridImpl onMouseExit(final NodeMouseExitHandler handler) {
        groupPrimitiveItem.onMouseExit(handler);
        return this;
    }

    @Override
    public ItemGridImpl show() {
        groupPrimitiveItem.show(new Runnable() {
            @Override
            public void run() {
                repositionItems();
                for (final DecoratedItem button : items) {
                    button.show();
                }
            }
        });
        return this;
    }

    @Override
    public ItemGridImpl onFocus(final Runnable callback) {
        groupPrimitiveItem.onFocus(callback);
        return this;
    }

    @Override
    public ItemGridImpl onUnFocus(final Runnable callback) {
        groupPrimitiveItem.onUnFocus(callback);
        return this;
    }

    public ItemGridImpl onRefresh(final Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
        return this;
    }

    public ItemGridImpl refresh() {
        return checkReposition();
    }

    @Override
    public ItemGridImpl hide() {
        groupPrimitiveItem.hide(new Runnable() {
            @Override
            public void run() {
                for (final DecoratedItem button : items) {
                    button.hide();
                }
                fireRefresh();
            }
        });
        return this;
    }

    @Override
    public void destroy() {
        groupPrimitiveItem.destroy();
        items.clear();
        refreshCallback = null;
    }

    public Point2DGrid getGrid() {
        return grid;
    }

    AbstractGroupItem getGroup() {
        return groupPrimitiveItem;
    }

    private ItemGridImpl checkReposition() {
        if (isVisible()) {
            return repositionItems();
        }
        return this;
    }

    private ItemGridImpl repositionItems() {
        final Iterator<Point2D> gridIterator = grid.iterator();
        for (final AbstractPrimitiveItem button : items) {
            final Point2D point = gridIterator.next();
            GWT.log("BUTTON AT = " + point);
            button.asPrimitive().setLocation(point);
        }
        fireRefresh();
        return this;
    }

    private void fireRefresh() {
        if (null != refreshCallback) {
            refreshCallback.run();
        }
    }

    @Override
    public Group asPrimitive() {
        return groupPrimitiveItem.asPrimitive();
    }

    private abstract static class ListItemsIterator<I extends AbstractPrimitiveItem> implements Iterator<DecoratedItem> {

        private final List<I> items;
        private int index;

        private ListItemsIterator(final List<I> items) {
            this.items = new LinkedList<>(items);
            this.index = 0;
        }

        protected abstract void remove(I item);

        @Override
        public boolean hasNext() {
            return index < items.size();
        }

        @Override
        public I next() {
            return items.get(index++ - 1);
        }

        @Override
        public void remove() {
            I item = items.get(index);
            remove(item);
        }
    }
}