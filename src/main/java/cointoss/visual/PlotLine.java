/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual;

import static java.lang.Double.*;
import static java.lang.Math.*;

import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;

/**
 * @version 2017/09/26 1:04:00
 */
final class PlotLine {
    private static final int moveto = 0, lineto = 1;

    private static final class A {
        int mode;// moveto 0,lineto 1

        double x, y;
    }

    boolean isx;

    private ArrayList<A> list = new ArrayList<>();

    private int length = 0;

    private int listsize = 0;

    private boolean over = false;

    private boolean neadmoveto = false;

    private double last = NaN;

    private double startv = NaN, minv = NaN, maxv = NaN, lastv = NaN;

    private double beforelast = NaN;

    private double beforeminv = NaN, beforemaxv = NaN, beforeendv = NaN;

    private int count = 0;

    public void setOrientationX(final boolean b) {
        isx = b;
    }

    public void init() {
        if (list == null) {
            list = new ArrayList<>();
            over = true;
        }
        length = 0;
        last = NaN;
        listsize = list.size();
        over = listsize == 0;
        count = 0;
        last = NaN;
        startv = NaN;
        minv = NaN;
        maxv = NaN;
        lastv = NaN;

        beforelast = NaN;
        beforeminv = NaN;
        beforemaxv = NaN;
        beforeendv = NaN;

    }

    public void clearMemory() {
        length = 0;
        if (list != null) {
            list.clear();
            list = null;
        }
        over = true;
    }

    private A get() {
        if (over) {
            final A a = new A();
            list.add(a);
            length++;
            return a;
        }

        final A a = list.get(length);
        length++;
        if (listsize == length) {
            over = true;
        }
        return a;
    }

    public void add(final int mode, final double x, final double y) {
        if (isx) {
            addX(mode, x, y);
        } else {
            addY(mode, x, y);
        }
    }

    public void toElements(final ObservableList<PathElement> elements) {
        if (isx) {
            addStoresX();
        } else {
            addStoresY();
        }

        final int elesize = elements.size();
        if (elesize > length) {
            elements.remove(length, elesize);
        }
        for (int index = 0; index < length; index++) {
            final A a = list.get(index);
            if (a.mode == lineto) {
                LineTo l;
                if (index < elesize) {
                    final PathElement e = elements.get(index);
                    if (e.getClass() == LineTo.class) {
                        l = (LineTo) e;
                    } else {
                        l = new LineTo();
                        elements.set(index, l);
                    }
                } else {
                    l = new LineTo();
                    elements.add(l);
                }
                l.setX(a.x);
                l.setY(a.y);
            } else {
                MoveTo l;
                if (index < elesize) {
                    final PathElement e = elements.get(index);
                    if (e.getClass() == MoveTo.class) {
                        l = (MoveTo) e;
                    } else {
                        l = new MoveTo();
                        elements.set(index, l);
                    }
                } else {
                    l = new MoveTo();
                    elements.add(l);
                }
                l.setX(a.x);
                l.setY(a.y);
            }
        }

    }

    private void addX(final int mode, final double x, final double y) {
        if (mode == moveto) {
            addStoresX();
            count = 0;
            last = NaN;
            startv = NaN;
            minv = NaN;
            maxv = NaN;
            lastv = NaN;

            beforelast = NaN;
            beforeminv = NaN;
            beforemaxv = NaN;
            beforeendv = NaN;
            neadmoveto = false;
            final A a = get();
            a.mode = moveto;
            a.x = x;
            a.y = y;
            return;
        }
        final double xx = floor(x * 2);
        final double yy = floor(y);
        if (last != xx) {
            addStoresX();
            last = xx;
            startv = y;
            maxv = yy;
            minv = yy;
            lastv = y;
            count = 1;
            return;
        }

        count++;
        lastv = y;
        maxv = Math.max(maxv, yy);
        minv = Math.min(minv, yy);
    }

    private void addStoresX() {
        if (count == 0) {
            return;
        }

        if (count == 1 || maxv == minv) {
            if (neadmoveto) {
                final A a = get();
                a.x = beforelast * 0.5;
                a.y = beforeendv;
                a.mode = moveto;
                neadmoveto = false;
            }
            final A a = get();
            a.x = last * 0.5;
            a.y = lastv;
            a.mode = lineto;
            beforelast = last;
            beforemaxv = lastv;
            beforeminv = lastv;
            beforeendv = lastv;
            return;
        }

        if (beforelast == beforelast) {

            final double d = beforeendv - maxv, b = beforeendv - minv;
            if (signum(d) * signum(b) > 0) {
                // linetoを追加
                if (neadmoveto) {
                    final A a = get();
                    a.x = beforelast * 0.5;
                    a.y = beforeendv;
                    a.mode = moveto;
                    neadmoveto = false;
                }
                final A a = get();
                a.x = last * 0.5;
                a.y = startv;
                a.mode = lineto;
            }
        }

        // movetoを追加
        A a = get();
        a.x = last * 0.5;
        a.y = minv;
        a.mode = moveto;

        // linetoを追加

        a = get();
        a.x = last * 0.5;
        a.y = maxv;
        a.mode = lineto;

        beforelast = last;
        beforemaxv = maxv;
        beforeminv = minv;
        beforeendv = lastv;
        neadmoveto = true;
        count = 0;
    }

    private void addY(final int mode, final double x, final double y) {
        if (mode == moveto) {
            addStoresY();
            count = 0;
            last = NaN;
            startv = NaN;
            minv = NaN;
            maxv = NaN;
            lastv = NaN;

            beforelast = NaN;
            beforeminv = NaN;
            beforemaxv = NaN;
            beforeendv = NaN;
            neadmoveto = false;
            final A a = get();
            a.mode = moveto;
            a.x = x;
            a.y = y;
            return;
        }
        final double xx = floor(x);
        final double yy = floor(y * 2);
        if (last != yy) {
            addStoresY();
            last = yy;
            startv = x;
            maxv = xx;
            minv = xx;
            lastv = x;
            count = 1;
            return;
        }

        count++;
        lastv = x;
        maxv = Math.max(maxv, xx);
        minv = Math.min(minv, xx);
    }

    private void addStoresY() {
        if (count == 0) {
            return;
        }

        if (count == 1 || maxv == minv) {
            if (neadmoveto) {
                final A a = get();
                a.y = beforelast * 0.5;
                a.x = beforeendv;
                a.mode = moveto;
                neadmoveto = false;
            }
            final A a = get();
            a.y = last * 0.5;
            a.x = lastv;
            a.mode = lineto;
            beforelast = last;
            beforemaxv = lastv;
            beforeminv = lastv;
            beforeendv = lastv;
            return;
        }

        if (beforelast == beforelast) {

            final double d = beforeendv - maxv, b = beforeendv - minv;
            if (signum(d) * signum(b) > 0) {
                // linetoを追加
                if (neadmoveto) {
                    final A a = get();
                    a.y = beforelast * 0.5;
                    a.x = beforeendv;
                    a.mode = moveto;
                    neadmoveto = false;
                }
                final A a = get();
                a.y = last * 0.5;
                a.x = startv;
                a.mode = lineto;
            }
        }

        // movetoを追加
        A a = get();
        a.y = last * 0.5;
        a.x = minv;
        a.mode = moveto;

        // linetoを追加

        a = get();
        a.y = last * 0.5;
        a.x = maxv;
        a.mode = lineto;

        beforelast = last;
        beforemaxv = maxv;
        beforeminv = minv;
        beforeendv = lastv;
        neadmoveto = true;
        count = 0;
    }
}
