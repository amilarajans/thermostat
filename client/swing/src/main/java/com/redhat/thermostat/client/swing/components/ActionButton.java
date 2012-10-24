/*
 * Copyright 2012 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.client.swing.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class ActionButton extends JButton implements ToolbarButton {
        
    public ActionButton(final Icon icon) {
        this(icon, "");
    }
    
    public ActionButton(final Icon icon, String text) {
        super(icon);
                
        setText(text);
        
        setUI(new ActionButtonUI());
        setOpaque(false);
        setContentAreaFilled(false);
        setBorder(new ToolbarButtonBorder(this));
    }
        
    @Override
    public AbstractButton getToolbarButton() {
        return this;
    }
    
    @Override
    public ToolbarButton copy() {
        ActionButton copy = new ActionButton(getIcon(), getText());
        copy.setName(getName());
        copy.setToolTipText(getToolTipText());
        return copy;
    }
    
    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            
            @Override
            public void run() {
               JFrame frame = new JFrame();
               frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
               
               HeaderPanel header = new HeaderPanel();
               header.setHeader("Test");
               
               Icon icon = new Icon() {
                
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    g.setColor(Color.CYAN);
                    g.fillRect(x, y, 16, 16);

                }
                
                @Override
                public int getIconWidth() {
                    // TODO Auto-generated method stub
                    return 16;
                }
                
                @Override
                public int getIconHeight() {
                    // TODO Auto-generated method stub
                    return 16;
                }
            }; 
               
               header.addToolBarButton(new ActionButton(icon, "Fluff"));
               
               frame.getContentPane().add(header);
               frame.setSize(500, 500);
               frame.setVisible(true);
            }
        });
    }      
}
