/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package arcadeflex.v037b7.machine;

//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.mame.cpuintrfH.*;
//platform imports
import static arcadeflex.v037b7.platform.osdepend.*;

public class buggychl {

    static /*unsigned*/ char from_main, from_mcu;
    static int mcu_sent = 0, main_sent = 0;

    /**
     * *************************************************************************
     *
     * Buggy CHallenge 68705 protection interface
     *
     * This is accurate. FairyLand Story seems to be identical.
     *
     **************************************************************************
     */
    static /*unsigned*/ char portA_in, portA_out, ddrA;

    public static ReadHandlerPtr buggychl_68705_portA_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            //logerror("%04x: 68705 port A read %02x\n",cpu_get_pc(),portA_in);
            return (portA_out & ddrA) | (portA_in & ~ddrA);
        }
    };

    public static WriteHandlerPtr buggychl_68705_portA_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //logerror("%04x: 68705 port A write %02x\n",cpu_get_pc(),data);
            portA_out = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr buggychl_68705_ddrA_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            ddrA = (char) (data & 0xFF);
        }
    };

    /*
	 *  Port B connections:
	 *  parts in [ ] are optional (not used by buggychl)
	 *
	 *  all bits are logical 1 when read (+5V pullup)
	 *
	 *  0   n.c.
	 *  1   W  IRQ ack and enable latch which holds data from main Z80 memory
	 *  2   W  loads latch to Z80
	 *  3   W  to Z80 BUSRQ (put it on hold?)
	 *  4   W  n.c.
	 *  5   W  [selects Z80 memory access direction (0 = write 1 = read)]
	 *  6   W  [loads the latch which holds the low 8 bits of the address of
	 *               the main Z80 memory location to access]
	 *  7   W  [loads the latch which holds the high 8 bits of the address of
	 *               the main Z80 memory location to access]
     */
    static /*unsigned*/ char portB_in, portB_out, ddrB;

    public static ReadHandlerPtr buggychl_68705_portB_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (portB_out & ddrB) | (portB_in & ~ddrB);
        }
    };

    public static WriteHandlerPtr buggychl_68705_portB_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("%04x: 68705 port B write %02x\n", cpu_get_pc(), data);

            if ((ddrB & 0x02) != 0 && (~data & 0x02) != 0 && (portB_out & 0x02) != 0) {
                portA_in = from_main;
                if (main_sent != 0) {
                    cpu_set_irq_line(2, 0, CLEAR_LINE);
                }
                main_sent = 0;
                logerror("read command %02x from main cpu\n", portA_in);
            }
            if ((ddrB & 0x04) != 0 && (data & 0x04) != 0 && (~portB_out & 0x04) != 0) {
                logerror("send command %02x to main cpu\n", portA_out);
                from_mcu = (char) (portA_out & 0xFF);
                mcu_sent = 1;
            }

            portB_out = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr buggychl_68705_ddrB_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            ddrB = (char) (data & 0xFF);
        }
    };

    /*
	 *  Port C connections:
	 *
	 *  all bits are logical 1 when read (+5V pullup)
	 *
	 *  0   R  1 when pending command Z80.68705
	 *  1   R  0 when pending command 68705.Z80
     */
    static /*unsigned*/ char portC_in, portC_out, ddrC;

    public static ReadHandlerPtr buggychl_68705_portC_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            portC_in = 0;
            if (main_sent != 0) {
                portC_in |= 0x01;
            }
            if (mcu_sent == 0) {
                portC_in |= 0x02;
            }
            logerror("%04x: 68705 port C read %02x\n", cpu_get_pc(), portC_in);
            return (portC_out & ddrC) | (portC_in & ~ddrC);
        }
    };

    public static WriteHandlerPtr buggychl_68705_portC_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("%04x: 68705 port C write %02x\n", cpu_get_pc(), data);
            portC_out = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr buggychl_68705_ddrC_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            ddrC = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr buggychl_mcu_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("%04x: mcu_w %02x\n", cpu_get_pc(), data);
            from_main = (char) (data & 0xFF);
            main_sent = 1;
            cpu_set_irq_line(2, 0, ASSERT_LINE);
        }
    };

    public static ReadHandlerPtr buggychl_mcu_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            logerror("%04x: mcu_r %02x\n", cpu_get_pc(), from_mcu);
            mcu_sent = 0;
            return from_mcu;
        }
    };

    public static ReadHandlerPtr buggychl_mcu_status_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res = 0;

            /* bit 0 = when 1, mcu is ready to receive data from main cpu */
 /* bit 1 = when 1, mcu has sent data to the main cpu */
            //logerror("%04x: mcu_status_r\n",cpu_get_pc());
            if (main_sent == 0) {
                res |= 0x01;
            }
            if (mcu_sent != 0) {
                res |= 0x02;
            }

            return res;
        }
    };
}
