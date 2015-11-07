/********************************************************************************
 * The contents of this file are subject to the GNU General Public License      *
 * (GPL) Version 2 or later (the "License"); you may not use this file except   *
 * in compliance with the License. You may obtain a copy of the License at      *
 * http://www.gnu.org/copyleft/gpl.html                                         *
 *                                                                              *
 * Software distributed under the License is distributed on an "AS IS" basis,   *
 * without warranty of any kind, either expressed or implied. See the License   *
 * for the specific language governing rights and limitations under the         *
 * License.                                                                     *
 *                                                                              *
 * This file was originally developed as part of the software suite that        *
 * supports the book "The Elements of Computing Systems" by Nisan and Schocken, *
 * MIT Press 2005. If you modify the contents of this file, please document and *
 * mark your changes clearly, for the benefit of others.                        *
 ********************************************************************************/

package SimulatorsGUI;

import Hack.CPUEmulator.*;
import Hack.ComputerParts.BusGUI;
import Hack.ComputerParts.PointedMemoryGUI;
import Hack.ComputerParts.RegisterGUI;
import HackGUI.BusComponent;
import HackGUI.PointedMemoryComponent;
import HackGUI.RegisterComponent;

import java.awt.*;
import java.io.File;

/**
 * This class represents the gui of the CPUEmulator.
 */
public class MultiCoreCPUEmulatorComponent extends CPUEmulatorComponent {

    // Creating the RegisterComponents a, d and pc.
    private RegisterComponent[] a;
    private RegisterComponent[] d;
    private RegisterComponent[] pc;

    // The ALU of the CPUEmulator.
    private ALUComponent[] alu;

    // The bus of the CPUEmulator.
    private BusComponent[] bus;

    /**
     * Constructs a new CPUEmulatorComponent.
     */
    public MultiCoreCPUEmulatorComponent( int numCores ) {
        super();

        this.numCores = numCores;

        alu = new ALUComponent[numCores];
        a = new RegisterComponent[numCores];
        d = new RegisterComponent[numCores];
        pc = new RegisterComponent[numCores];
        bus = new BusComponent[numCores];

        for( int i = 0; i  < numCores; i++ ) {
            alu[i] = new ALUComponent();
            alu[i].setName("ALU"+i);
            a[i] = new RegisterComponent();
            d[i] = new RegisterComponent();
            pc[i] = new RegisterComponent();
            bus[i] = new BusComponent();
        }
        setRegistersNames();
        jbInit();
    }

    // Sets the names of the registers.
    private void setRegistersNames() {

        for( int i = 0; i  < numCores; i++ ) {
            a[i].setName("A" + i);
            d[i].setName("D" + i);
            pc[i].setName("PC" + i);
        }
    }

    public Point getAdditionalDisplayLocation() {
        return new Point(476, 25);
    }

    /**
     * Returns the alu GUI component.
     */
    public ALUGUI getALU( int i ) {
        return alu[i];
    }

    /**
     * Returns the bus GUI component.
     */
    public BusGUI getBus( int i ) {
        return bus[i];
    }

    /**
     * Returns the screen GUI component.
     */
    public RegisterGUI getA( int i ) {
        return a[i];
    }

    /**
     * Returns the screen GUI component.
     */
    public RegisterGUI getD( int i ) {
        return d[i];
    }

    /**
     * Returns the screen GUI component.
     */
    public RegisterGUI getPC( int i ) {
        return pc[i];
    }

    // Initialization of this component.
    protected void jbInit() {
        for( int i = 0; i < numCores; i++ ) {
            pc[i].setBounds(new Rectangle(35, 527 + i * pc[i].getHeight(), pc[i].getWidth(), pc[i].getHeight()));
            a[i].setBounds(new Rectangle(278, 527 + i * a[i].getHeight(), a[i].getWidth(), a[i].getHeight()));
            d[i].setBounds(new Rectangle(646, 351, d[i].getWidth(), d[i].getHeight()));
            d[i].setBounds(new Rectangle(646, 351 + i * d[i].getHeight(), d[i].getWidth(), d[i].getHeight()));
            alu[i].setBounds(new Rectangle(551, 414, alu[i].getWidth(), alu[i].getHeight()));

            bus[i].setBounds(new Rectangle(0, 0, EMULATOR_WIDTH, EMULATOR_HEIGHT));

            this.add(bus[i], null);
            this.add(a[i], null);
            this.add(pc[i], null);
            this.add(alu[i], null);
            this.add(d[i], null);
        }

        super.jbInit();
    }
}
