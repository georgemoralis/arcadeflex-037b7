/*
 * ported to v0.37b7
 */
package arcadeflex.v037b7.machine;

//common imports
import static arcadeflex.common.ptrLib.*;
import static arcadeflex.common.libc.cstring.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;

public class exctsccr {

    public static int MCU_KEY_TABLE_SIZE = 16;

    /* These are global */
    public static UBytePtr exctsccr_mcu_ram = new UBytePtr();

    /* Local stuff */
    static int mcu_code_latch;

    /* input = 0x6009 - data = 0x6170 */
    static int mcu_table1[] = {
        0x23, 0x05, 0xfb, 0x07, 0x4d, 0x3b, 0x7d, 0x03,
        0x9a, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    /* input = 0x600d - data = 0x61f0 */
    static int mcu_table2[] = {
        0x75, 0x25, 0x4b, 0x10, 0xa6, 0x06, 0x9a, 0x02,
        0x04, 0x11, 0x09, 0x09, 0x2a, 0x10, 0x3a, 0x10
    };

    /* input = ?????? - data = 0x6300 */
    static int mcu_table3[] = {
        0xba, 0x26, 0x53, 0x0d, 0x01, 0x24, 0x1d, 0x15,
        0xd0, 0x1d, 0x35, 0x1f, 0x32, 0x22, 0xa0, 0x2c,
        0x5e, 0x2e, 0x92, 0x2e, 0xf9, 0x2e, 0x43, 0x2f,
        0x59, 0x2f, 0x6f, 0x2f, 0xd1, 0x38, 0xd8, 0x3e,
        0x9e, 0x0d, 0x4c, 0x47, 0x1f, 0x1f, 0x5a, 0x1f,
        0xbe, 0x22, 0xa2, 0x23, 0x33, 0x35, 0x69, 0x34,
        0x7c, 0x3a, 0x00, 0x00, 0xdc, 0x44, 0x78, 0x0b,
        0x65, 0x0d, 0xc4, 0xb2, 0xc4, 0xb2, 0xc4, 0xb2
    };

    /* input = 0x6007 - data = 0x629b */
    static int mcu_table4[] = {
        0xc5, 0x24, 0xc5, 0x24, 0x27, 0x3f, 0xd0, 0x07,
        0x07, 0x0b, 0xd8, 0x02, 0x9a, 0x08, 0x00, 0x00
    };

    /* input = 0x6007 - data = 0x629b */
    static int mcu_table5[] = {
        0x54, 0x43, 0xd0, 0x07, 0xd8, 0x02, 0x9a, 0x08,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    /* input = 0x6007 - data = 0x629b */
    static int mcu_table6[] = {
        0x2b, 0x03, 0xd0, 0x07, 0xd8, 0x02, 0x9a, 0x08,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    /* input = 0x6007 - data = 0x629b */
    static int mcu_table7[] = {
        0xfa, 0x0e, 0x9a, 0x08, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    /* input = 0x6011 - data = 0x607f */
    static int mcu_table8[] = {
        0x0b, 0x78, 0x47, 0x04, 0x36, 0x18, 0x23, 0xf1,
        0x10, 0xee, 0x16, 0x04, 0x11, 0x94, 0x16, 0xdd,
        0x13, 0x4e, 0x06, 0xa6, 0x13, 0x12, 0x08, 0x9a,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,};

    static void mcu_sort_list(UBytePtr src, UBytePtr dst) {
        int i;

        for (i = 0; i < 0x20; i++) {
            int where = src.read(i);

            if (where < 0x20) {
                int offs = (i << 1);

                where <<= 1;
                dst.write(where, mcu_table3[offs]);
                dst.write(where + 1, mcu_table3[offs + 1]);
            }
        }
    }

    public static WriteHandlerPtr exctsccr_mcu_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (data == 0xff) {
                /* goes around the mcu checks */
                exctsccr_mcu_ram.write(0x0003, 0x01);
                /* mcu state = running */

                exctsccr_mcu_ram.write(0x0005, 0x08);
                exctsccr_mcu_ram.write(0x0380, 0xf1);
                exctsccr_mcu_ram.write(0x0381, 0x01);
                exctsccr_mcu_ram.write(0x02f8, 0x1f);
                /* Selects song to play during gameplay */

                exctsccr_mcu_ram.write(0x0009, 0x08);
                memcpy(exctsccr_mcu_ram, 0x0170, mcu_table1, MCU_KEY_TABLE_SIZE);

                if (exctsccr_mcu_ram.read(0x000d) == 0x01) {
                    exctsccr_mcu_ram.write(0x000d, 0x08);

                    memcpy(exctsccr_mcu_ram, 0x01f0, mcu_table2, MCU_KEY_TABLE_SIZE);
                }

                mcu_sort_list(new UBytePtr(exctsccr_mcu_ram, 0x03e0), new UBytePtr(exctsccr_mcu_ram, 0x0300));

                exctsccr_mcu_ram.write(0x02fb, 0x7e);
                exctsccr_mcu_ram.write(0x02fd, 0x7e);

                if (mcu_code_latch != 0) {
                    exctsccr_mcu_ram.write(0x02f9, 0x7f);
                    exctsccr_mcu_ram.write(0x02fa, 0x0d);
                    exctsccr_mcu_ram.write(0x02fc, 0x05);
                    exctsccr_mcu_ram.write(0x02fe, 0x01);
                } else {
                    exctsccr_mcu_ram.write(0x02f9, 0x81);
                    exctsccr_mcu_ram.write(0x02fa, 0x2d);
                    exctsccr_mcu_ram.write(0x02fc, 0x25);
                    exctsccr_mcu_ram.write(0x02fe, 0x09);
                }

                if (exctsccr_mcu_ram.read(0x000f) == 0x02) {
                    exctsccr_mcu_ram.write(0x000f, 0x08);
                    exctsccr_mcu_ram.write(0x02f6, 0xd6);
                    exctsccr_mcu_ram.write(0x02f7, 0x39);
                }

                if (exctsccr_mcu_ram.read(0x0007) == 0x02) {
                    exctsccr_mcu_ram.write(0x0007, 0x08);

                    if (exctsccr_mcu_ram.read(0x0204) > 3) {
                        exctsccr_mcu_ram.write(0x0204, 0);
                    }

                    switch (exctsccr_mcu_ram.read(0x0204)) {
                        case 0x00:
                            memcpy(exctsccr_mcu_ram, 0x029b, mcu_table4, MCU_KEY_TABLE_SIZE);
                            break;

                        case 0x01:
                            memcpy(exctsccr_mcu_ram, 0x029b, mcu_table5, MCU_KEY_TABLE_SIZE);
                            break;

                        case 0x02:
                            memcpy(exctsccr_mcu_ram, 0x029b, mcu_table6, MCU_KEY_TABLE_SIZE);
                            break;

                        case 0x03:
                            memcpy(exctsccr_mcu_ram, 0x029b, mcu_table7, MCU_KEY_TABLE_SIZE);
                            break;
                    }

                    if (exctsccr_mcu_ram.read(0x0204) != 3) {
                        exctsccr_mcu_ram.write(0x0204, exctsccr_mcu_ram.read(0x0204) + 1);
                    }
                }

                exctsccr_mcu_ram.write(0x0011, 0x08);
                memcpy(exctsccr_mcu_ram, 0x007f, mcu_table8, MCU_KEY_TABLE_SIZE * 2);
            }
        }
    };

    public static WriteHandlerPtr exctsccr_mcu_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (offset == 0x02f9) {
                mcu_code_latch = data;
            }

            exctsccr_mcu_ram.write(offset, data);
        }
    };
}
