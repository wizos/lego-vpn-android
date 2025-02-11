package cn.forward.androids.Image;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;

import java.lang.ref.WeakReference;

import cn.forward.androids.SimpleAsyncTask;


public class AsyncDrawable extends Drawable {

    private final WeakReference<SimpleAsyncTask> mImageLoadTaskRef;

    private final Drawable baseDrawable;

    public AsyncDrawable(Drawable drawable, SimpleAsyncTask task) {
        if (task == null) {
            throw new IllegalArgumentException("SimpleAsyncTask is null!");
        }
        baseDrawable = drawable;
        mImageLoadTaskRef = new WeakReference<SimpleAsyncTask>(task);
    }

    public SimpleAsyncTask getBitmapWorkerTask() {
        return mImageLoadTaskRef.get();
    }

    @Override
    public void draw(Canvas canvas) {
        if (baseDrawable != null) {
            baseDrawable.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int i) {
        if (baseDrawable != null) {
            baseDrawable.setAlpha(i);
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (baseDrawable != null) {
            baseDrawable.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        return baseDrawable == null ? PixelFormat.UNKNOWN : baseDrawable.getOpacity();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        if (baseDrawable != null) {
            baseDrawable.setBounds(left, top, right, bottom);
        }
    }

    @Override
    public void setBounds(Rect bounds) {
        if (baseDrawable != null) {
            baseDrawable.setBounds(bounds);
        }
    }

    @Override
    public void setChangingConfigurations(int configs) {
        if (baseDrawable != null) {
            baseDrawable.setChangingConfigurations(configs);
        }
    }

    @Override
    public int getChangingConfigurations() {
        return baseDrawable == null ? 0 : baseDrawable.getChangingConfigurations();
    }

    @Override
    public void setDither(boolean dither) {
        if (baseDrawable != null) {
            baseDrawable.setDither(dither);
        }
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        if (baseDrawable != null) {
            baseDrawable.setFilterBitmap(filter);
        }
    }

    @Override
    public void invalidateSelf() {
        if (baseDrawable != null) {
            baseDrawable.invalidateSelf();
        }
    }

    @Override
    public void scheduleSelf(Runnable what, long when) {
        if (baseDrawable != null) {
            baseDrawable.scheduleSelf(what, when);
        }
    }

    @Override
    public void unscheduleSelf(Runnable what) {
        if (baseDrawable != null) {
            baseDrawable.unscheduleSelf(what);
        }
    }

    @Override
    public void setColorFilter(int color, PorterDuff.Mode mode) {
        if (baseDrawable != null) {
            baseDrawable.setColorFilter(color, mode);
        }
    }

    @Override
    public void clearColorFilter() {
        if (baseDrawable != null) {
            baseDrawable.clearColorFilter();
        }
    }

    @Override
    public boolean isStateful() {
        return baseDrawable != null && baseDrawable.isStateful();
    }

    @Override
    public boolean setState(int[] stateSet) {
        return baseDrawable != null && baseDrawable.setState(stateSet);
    }

    @Override
    public int[] getState() {
        return baseDrawable == null ? null : baseDrawable.getState();
    }

    @Override
    public Drawable getCurrent() {
        return baseDrawable == null ? null : baseDrawable.getCurrent();
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        return baseDrawable != null && baseDrawable.setVisible(visible, restart);
    }

    @Override
    public Region getTransparentRegion() {
        return baseDrawable == null ? null : baseDrawable.getTransparentRegion();
    }

    @Override
    public int getIntrinsicWidth() {
        return baseDrawable == null ? 0 : baseDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return baseDrawable == null ? 0 : baseDrawable.getIntrinsicHeight();
    }

    @Override
    public int getMinimumWidth() {
        return baseDrawable == null ? 0 : baseDrawable.getMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        return baseDrawable == null ? 0 : baseDrawable.getMinimumHeight();
    }

    @Override
    public boolean getPadding(Rect padding) {
        return baseDrawable != null && baseDrawable.getPadding(padding);
    }

    @Override
    public Drawable mutate() {
        return baseDrawable == null ? null : baseDrawable.mutate();
    }

    @Override
    public ConstantState getConstantState() {
        return baseDrawable == null ? null : baseDrawable.getConstantState();
    }
}
