/** jbead - http://www.brunoldsoftware.ch
    Copyright (C) 2001-2012  Damian Brunold

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.jbead;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SimulationPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    private int offsetx;
    private int left;
    private int maxj;
    private int w;

    public SimulationPanel(Model model, Selection selection, final BeadForm form) {
        super(model, selection);
        model.addListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                form.simulationMouseUp(e);
            }
        });
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension((getVisibleWidth() + 1) * gridx, 3 * gridy);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getMinimumSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        offsetx = getOffsetX();
        left = getLeft();
        maxj = getMaxj();
        w = getVisibleWidth();
        paintBeads(g);
    }

    private int getOffsetX() {
        return (getWidth() - 1 - (model.getWidth() + 1) * gridx / 2 + gridx / 2) / 2;
    }

    private int getLeft() {
        int left = offsetx;
        if (left < 0) left = gridx / 2;
        return left;
    }

    private int getMaxj() {
        int maxj = Math.min(model.getHeight(), getHeight() / gridy + 1);
        return maxj;
    }

    private int getVisibleWidth() {
        return model.getWidth() / 2;
    }

    private int x(int i) {
        return left + i * gridx;
    }

    private int y(int j) {
        return getHeight() - 1 - (j + 1) * gridy;
    }

    private void paintBeads(Graphics g) {
        int width = model.getWidth();
        int shift = model.getShift();
        for (int j = 0; j < model.getHeight(); j++) {
            for (int i = 0; i < width; i++) {
                byte c = model.get(new Point(i, j).scrolled(scroll));
                int idx = i + width * j + shift;
                int i1 = idx % width;
                int j1 = idx / width;
                int ii = correctCoordinatesX(i1, j1);
                int jj = correctCoordinatesY(i1, j1);
                if (y(jj) < -gridy) return;
                if (ii > w && ii != width) continue;
                if (scroll % 2 == 0) {
                    if (jj % 2 == 0) {
                        if (ii == w) continue;
                        paintBead(g, left + ii * gridx, getHeight() - 1 - (jj + 1) * gridy, gridx, gridy, model.getColor(c));
                    } else {
                        if (ii != width && ii != w) {
                            paintBead(g, left - gridx / 2 + ii * gridx, getHeight() - 1 - (jj + 1) * gridy, gridx, gridy, model.getColor(c));
                        } else if (ii == width) {
                            paintBead(g, left - gridx / 2, getHeight() - 1 - (jj + 2) * gridy, gridx / 2, gridy, model.getColor(c));
                        } else {
                            paintBead(g, left - gridx / 2 + ii * gridx, getHeight() - 1 - (jj + 1) * gridy, gridx / 2, gridy, model.getColor(c));
                        }
                    }
                } else {
                    if (jj % 2 == 1) {
                        if (ii == w) continue;
                        paintBead(g, left + ii * gridx, getHeight() - 1 - (jj + 1) * gridy, gridx, gridy, model.getColor(c));
                    } else {
                        if (ii != width && ii != w) {
                            paintBead(g, left - gridx / 2 + ii * gridx, getHeight() - 1 - (jj + 1) * gridy, gridx, gridy, model.getColor(c));
                        } else if (ii == width) {
                            paintBead(g, left - gridx / 2, getHeight() - 1 - (jj + 2) * gridy, gridx / 2, gridy, model.getColor(c));
                        } else {
                            paintBead(g, left - gridx / 2 + ii * gridx, getHeight() - 1 - (jj + 1) * gridy, gridx / 2, gridy, model.getColor(c));
                        }
                    }
                }
            }
        }
    }

    private void paintBead(Graphics g, int i, int j, int w, int h, Color color) {
        g.setColor(color);
        g.fillRect(i + 1, j + 1, w - 1, h - 1);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(i, j, w, h);
    }

    int correctCoordinatesX(int _i, int _j) {
        int idx = _i + (_j + scroll) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = m1 + 1;
        int k = 0;
        int m = m1 ;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        return idx;
    }

    int correctCoordinatesY(int _i, int _j) {
        int idx = _i + (_j + scroll) * model.getWidth();
        int m1 = model.getWidth();
        int m2 = m1 + 1;
        int k = 0;
        int m = m1;
        while (idx >= m) {
            idx -= m;
            k++;
            m = (k % 2 == 0) ? m1 : m2;
        }
        return k - scroll;
    }

    public void redraw(int _i, int _j) {
        if (!isVisible()) return;

        byte c = model.get(new Point(_i, _j).scrolled(scroll));
        assert (c >= 0 && c <= 9);

        int ii = _i;
        int jj = _j;

        int idx = ii + model.getWidth() * jj + model.getShift();
        int i1 = idx % model.getWidth();
        int j1 = idx / model.getWidth();
        _i = correctCoordinatesX(i1, j1);
        _j = correctCoordinatesY(i1, j1);

        Graphics g = getGraphics();
        g.setColor(model.getColor(c));
        int left = offsetx;
        int w = getVisibleWidth();
        if (_i > w && _i != model.getWidth()) return;
        if (scroll % 2 == 0) {
            if (_j % 2 == 0) {
                if (_i == w) return;
                g.fillRect(left + _i * gridx + 1, getHeight() - (_j + 1) * gridy, gridx - 1, gridy - 1);
            } else {
                if (_i != model.getWidth() && _i != w) {
                    g.fillRect(left - gridx / 2 + _i * gridx + 1, getHeight() - (_j + 1) * gridy, gridx - 1, gridy - 1);
                } else if (_i == w) {
                    g.fillRect(left - gridx / 2 + _i * gridx + 1, getHeight() - (_j + 1) * gridy, gridx / 2 - 1, gridy - 1);
                } else {
                    g.fillRect(left - gridx / 2 + 1, getHeight() - (_j + 2) * gridy, gridx / 2 - 1, gridy - 1);
                }
            }
        } else {
            if (_j % 2 == 1) {
                if (_i == w) return;
                g.fillRect(left + _i * gridx + 1, getHeight() - (_j + 1) * gridy, gridx - 1, gridy - 1);
            } else {
                if (_i != model.getWidth() && _i != w) {
                    g.fillRect(left - gridx / 2 + _i * gridx + 1, getHeight() - (_j + 1) * gridy, gridx - 1, gridy - 1);
                } else if (_i == w) {
                    g.fillRect(left - gridx / 2 + _i * gridx + 1, getHeight() - (_j + 1) * gridy, gridx / 2 - 1, gridy - 1);
                } else {
                    g.fillRect(left - gridx / 2 + 1, getHeight() - (_j + 2) * gridy, gridx / 2 - 1, gridy - 1);
                }
            }
        }
        g.dispose();
    }

    @Override
    public void redraw(Point pt) {
        redraw(pt.getX(), pt.getY() - scroll);
    }

    @Override
    public void shiftChanged(int scroll) {
        repaint();
    }

    boolean mouseToField(Point pt) {
        int w = getVisibleWidth();
        int shift = model.getShift();
        int _i = pt.getX();
        int _j = pt.getY();
        int i;
        int jj = (getHeight() - _j) / gridy;
        if (scroll % 2 == 0) {
            if (jj % 2 == 0) {
                if (_i < offsetx || _i > offsetx + w * gridx) return false;
                i = (_i - offsetx) / gridx;
            } else {
                if (_i < offsetx - gridx / 2 || _i > offsetx + w * gridx + gridx / 2) return false;
                i = (_i - offsetx + gridx / 2) / gridx;
            }
        } else {
            if (jj % 2 == 1) {
                if (_i < offsetx || _i > offsetx + w * gridx) return false;
                i = (_i - offsetx) / gridx;
            } else {
                if (_i < offsetx - gridx / 2 || _i > offsetx + w * gridx + gridx / 2) return false;
                i = (_i - offsetx + gridx / 2) / gridx;
            }
        }
        i -= shift;
        if (i >= model.getWidth()) {
            i -= model.getWidth();
            jj++;
        } else if (i < 0) {
            i += model.getWidth();
            jj--;
        }
        pt.setX(i);
        pt.setY(jj);
        return true;
    }

    public void togglePoint(Point pt) {
        if (!mouseToField(pt)) return;
        int idx = 0;
        int m1 = model.getWidth();
        int m2 = m1 + 1;
        for (int j = 0; j < pt.getY() + scroll; j++) {
            if (j % 2 == 0)
                idx += m1;
            else
                idx += m2;
        }
        idx += pt.getX();
        int j = idx / model.getWidth();
        int i = idx % model.getWidth();
        model.setPoint(new Point(i, j - scroll));
    }

}
