package me.matoosh.nerve.android.watch;

import android.view.View;

import androidx.core.widget.NestedScrollView;
import me.everything.android.ui.overscroll.adapters.IOverScrollDecoratorAdapter;

public class NestedScrollViewOverScrollDecorAdapter implements IOverScrollDecoratorAdapter {
    protected final NestedScrollView mView;

    public NestedScrollViewOverScrollDecorAdapter(NestedScrollView view) {
        this.mView = view;
    }

    public View getView() {
        return this.mView;
    }

    public boolean isInAbsoluteStart() {
        return !this.mView.canScrollVertically(-1);
    }

    public boolean isInAbsoluteEnd() {
        return !this.mView.canScrollVertically(1);
    }
}