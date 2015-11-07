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
public class SingleCoreCPUEmulatorComponent extends CPUEmulatorComponent {

    // Creating the RegisterComponents a, d and pc.
    private RegisterComponent a;
    private RegisterComponent d;
    private RegisterComponent pc;

    // The ALU of the CPUEmulator.
    private ALUComponent alu;

    // The bus of the CPUEmulator.
    private BusComponent bus;


    /**
     * Constructs a new CPUEmulatorComponent.
     */
    public SingleCoreCPUEmulatorComponent() {
        super();
        alu = new ALUComponent();
        a = new RegisterComponent();
        d = new RegisterComponent();
        pc = new RegisterComponent();
        setRegistersNames();
        bus = new BusComponent();
        jbInit();
    }

    // Sets the names of the registers.
    private void setRegistersNames() {
        a.setName("A");
        d.setName("D");
        pc.setName("PC");
    }

    /**
     * Returns the alu GUI component.
     */
    public ALUGUI getALU( int i ) {
        return alu;
    }

    /**
     * Returns the bus GUI component.
     */
    public BusGUI getBus( int i ) {
        return bus;
    }

    /**
     * Returns the screen GUI component.
     */
    public RegisterGUI getA( int i ) {
        return a;
    }

    /**
     * Returns the screen GUI component.
     */
    public RegisterGUI getD( int i ) {
        return d;
    }

    /**
     * Returns the screen GUI component.
     */
    public RegisterGUI getPC( int i ) {
        return pc;
    }

    // Initialization of this component.
    protected void jbInit() {
        pc.setBounds(new Rectangle(35, 527, pc.getWidth(), pc.getHeight()));
        a.setBounds(new Rectangle(278, 527, a.getWidth(), a.getHeight()));
        d.setBounds(new Rectangle(646, 351, d.getWidth(), d.getHeight()));
        alu.setBounds(new Rectangle(551, 414, alu.getWidth(), alu.getHeight()));
        bus.setBounds(new Rectangle(0, 0, EMULATOR_WIDTH , EMULATOR_HEIGHT));

        this.add(bus, null);
        this.add(a, null);
        this.add(pc, null);
        this.add(alu, null);
        this.add(d, null);

        super.jbInit();
    }
}
