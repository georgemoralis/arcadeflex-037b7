/*
 * ported to v0.37b7
 */
package arcadeflex.v037b7.machine;

//cpu imports
import static arcadeflex.v037b7.cpu.z80.z80H.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.mame.*;
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.commonH.*;

public class berzerk {

    public static int irq_enabled;
    public static int nmi_enabled;
    public static int berzerk_irq_end_of_screen;

    public static int berzerkplayvoice;

    public static int int_count;

    public static InitMachinePtr berzerk_init_machine = new InitMachinePtr() {
        public void handler() {
            int i;

            /* Berzerk expects locations 3800-3fff to be ff */
            for (i = 0x3800; i < 0x4000; i++) {
                memory_region(REGION_CPU1).write(i, 0xff);
            }

            irq_enabled = 0;
            nmi_enabled = 0;
            berzerk_irq_end_of_screen = 0;
            int_count = 0;
        }
    };

    public static WriteHandlerPtr berzerk_irq_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            irq_enabled = data;
        }
    };

    public static WriteHandlerPtr berzerk_nmi_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            nmi_enabled = 1;
        }
    };

    public static WriteHandlerPtr berzerk_nmi_disable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            nmi_enabled = 0;
        }
    };

    public static ReadHandlerPtr berzerk_nmi_enable_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            nmi_enabled = 1;
            return 0;
        }
    };

    public static ReadHandlerPtr berzerk_nmi_disable_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            nmi_enabled = 0;
            return 0;
        }
    };

    public static ReadHandlerPtr berzerk_led_on_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            set_led_status(0, 1);

            return 0;
        }
    };

    public static ReadHandlerPtr berzerk_led_off_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            set_led_status(0, 0);

            return 0;
        }
    };

    public static ReadHandlerPtr berzerk_voiceboard_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (berzerkplayvoice == 0) {
                return 0;
            } else {
                return 0x40;
            }
        }
    };

    public static InterruptPtr berzerk_interrupt = new InterruptPtr() {
        public int handler() {
            int_count++;

            switch (int_count) {
                default:
                case 4:
                case 8:
                    if (int_count == 8) {
                        berzerk_irq_end_of_screen = 0;
                        int_count = 0;
                    } else {
                        berzerk_irq_end_of_screen = 1;
                    }
                    return irq_enabled != 0 ? 0xfc : Z80_IGNORE_INT;

                case 1:
                case 2:
                case 3:
                case 5:
                case 6:
                case 7:
                    if (int_count == 7) {
                        berzerk_irq_end_of_screen = 1;
                    } else {
                        berzerk_irq_end_of_screen = 0;
                    }
                    return nmi_enabled != 0 ? Z80_NMI_INT : Z80_IGNORE_INT;
            }
        }
    };
}
