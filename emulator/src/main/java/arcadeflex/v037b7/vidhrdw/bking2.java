/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package arcadeflex.v037b7.vidhrdw;

//common imports
import static arcadeflex.common.ptrLib.*;
import static arcadeflex.common.libc.cstring.*;
import static arcadeflex.common.libc.expressions.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.inptport.*;
import static arcadeflex.v037b7.mame.osdependH.*;
import static arcadeflex.v037b7.mame.mame.*;
import static arcadeflex.v037b7.mame.drawgfx.*;
//sound imports
import static arcadeflex.v037b7.sound.mixer.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class bking2 {

    static int/*UINT8*/ xld1 = 0;
    static int/*UINT8*/ xld2 = 0;
    static int/*UINT8*/ xld3 = 0;
    static int/*UINT8*/ yld1 = 0;
    static int/*UINT8*/ yld2 = 0;
    static int/*UINT8*/ yld3 = 0;
    static int msk = 0;
    static int ball1_pic;
    static int ball2_pic;
    static int crow_pic;
    static int crow_flip;
    static int[] palette_bank = new int[1];
    static int controller;
    static int hitclr = 0;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * The palette PROM is connected to the RGB output this way:
     *
     * bit 7 -- 390 ohm resistor -- BLUE -- 220 ohm resistor -- BLUE -- 820 ohm
     * resistor -- GREEN -- 390 ohm resistor -- GREEN -- 220 ohm resistor --
     * GREEN -- 820 ohm resistor -- RED -- 390 ohm resistor -- RED bit 0 -- 220
     * ohm resistor -- RED
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr bking2_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            int p_ptr = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_ptr++] = (char) (0x92 * bit0 + 0x46 * bit1 + 0x27 * bit2);
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_ptr++] = (char) (0x92 * bit0 + 0x46 * bit1 + 0x27 * bit2);
                /* blue component */
                bit0 = (color_prom.read() >> 6) & 0x01;
                bit1 = (color_prom.read() >> 7) & 0x01;
                bit2 = 0;
                palette[p_ptr++] = (char) (0x92 * bit0 + 0x46 * bit1 + 0x27 * bit2);

                color_prom.inc();
            }

            /* color PROM A7-A8 is the palette select */
 /* character colors. Image bits go to A0-A2 of the color PROM */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (((i << 4) & 0x180) | (i & 0x07));
            }

            /* crow colors. Image bits go to A5-A6. */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (((i << 5) & 0x180) | ((i & 0x03) << 5));
            }

            /* ball colors. Ball 1 image bit goes to A3. Ball 2 to A4. */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (((i << 6) & 0x180) | ((i & 0x01) << 3));
                colortable[Machine.drv.gfxdecodeinfo[3].color_codes_start + i] = (char) (((i << 6) & 0x180) | ((i & 0x01) << 4));
            }
        }
    };

    public static WriteHandlerPtr bking2_xld1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            xld1 = (-data) & 0xFF;
        }
    };

    public static WriteHandlerPtr bking2_yld1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            yld1 = (-data) & 0xFF;
        }
    };

    public static WriteHandlerPtr bking2_xld2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            xld2 = (-data) & 0xFF;
        }
    };

    public static WriteHandlerPtr bking2_yld2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            yld2 = (-data) & 0xFF;
        }
    };

    public static WriteHandlerPtr bking2_xld3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            xld3 = (-data) & 0xFF;
        }
    };

    public static WriteHandlerPtr bking2_yld3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            yld3 = (-data) & 0xFF;
        }
    };

    public static WriteHandlerPtr bking2_msk_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            msk = data;
        }
    };

    public static WriteHandlerPtr bking2_cont1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* D0 = COIN LOCK */
 /* D1 =  BALL 5 (Controller selection) */
 /* D2 = VINV (flip screen) */
 /* D3 = Not Connected */
 /* D4-D7 = CROW0-CROW3 (selects crow picture) */

            coin_lockout_global_w.handler(0, ~data & 0x01);

            flip_screen_w.handler(0, data & 0x04);

            controller = data & 0x02;

            crow_pic = (data >> 4) & 0x0f;
        }
    };
    static int last = -1;
    public static WriteHandlerPtr bking2_cont2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            /* D0-D2 = BALL10 - BALL12 (Selects player 1 ball picture) */
 /* D3-D5 = BALL20 - BALL22 (Selects player 2 ball picture) */
 /* D6 = HIT1 */
 /* D7 = HIT2 */
            ball1_pic = data & 0x07;
            ball2_pic = (data >> 3) & 0x07;

            if ((last & 0xc0) != (data & 0xc0)) {
                //debug_key_pressed = 1;
                last = data;
            }
        }
    };

    public static WriteHandlerPtr bking2_cont3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* D0 = CROW INV (inverts Crow picture and coordinates) */
 /* D1-D2 = COLOR 0 - COLOR 1 (switches 4 color palettes, global across all graphics) */
 /* D3 = SOUND STOP */

            crow_flip = ~data & 0x01;

            set_vh_global_attribute(palette_bank, (data >> 1) & 0x03);

            mixer_sound_enable_global_w(~data & 0x08);
        }
    };

    public static WriteHandlerPtr bking2_hitclr_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            hitclr = data;
        }
    };

    public static ReadHandlerPtr bking2_input_port_5_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return controller != 0 ? input_port_7_r.handler(0) : input_port_5_r.handler(0);
        }
    };

    public static ReadHandlerPtr bking2_input_port_6_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return controller != 0 ? input_port_8_r.handler(0) : input_port_6_r.handler(0);
        }
    };

    /* Hack alert.  I don't know how to upper bits work, so I'm just returning
	   what the code expects, otherwise the collision detection is skipped */
    public static ReadHandlerPtr bking2_pos_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            char pos, x, y;

            if ((hitclr & 0x04) != 0) {
                x = (char) xld2;
                y = (char) yld2;
            } else {
                x = (char) xld1;
                y = (char) yld1;
            }

            pos = (char) (((y >> 3 << 5) | (x >> 3)) + 2);

            switch (offset) {
                case 0x00:
                    return (pos & 0x0f) << 4;
                case 0x08:
                    return (pos & 0xf0);
                case 0x10:
                    return ((pos & 0x0300) >> 4) | (RAM.read(0x804c) & 0xc0);
                default:
                    return 0;
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdatePtr bking2_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            if (full_refresh != 0) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer[offs] != 0 || dirtybuffer[offs + 1] != 0) {
                    int sx, sy;
                    int flipx, flipy;

                    dirtybuffer[offs] = dirtybuffer[offs + 1] = 0;

                    sx = (offs / 2) % 32;
                    sy = (offs / 2) / 32;
                    flipx = videoram.read(offs + 1) & 0x04;
                    flipy = videoram.read(offs + 1) & 0x08;

                    if (flip_screen() != 0) {
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);

                        sx = 31 - sx;
                        sy = 31 - sy;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + ((videoram.read(offs + 1) & 0x03) << 8),
                            palette_bank[0],
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            Machine.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);

            /* draw the balls */
            drawgfx(bitmap, Machine.gfx[2],
                    ball1_pic,
                    palette_bank[0],
                    0, 0,
                    xld1, yld1,
                    Machine.visible_area, TRANSPARENCY_PEN, 0);

            drawgfx(bitmap, Machine.gfx[3],
                    ball2_pic,
                    palette_bank[0],
                    0, 0,
                    xld2, yld2,
                    Machine.visible_area, TRANSPARENCY_PEN, 0);

            /* draw the crow */
            drawgfx(bitmap, Machine.gfx[1],
                    crow_pic,
                    palette_bank[0],
                    crow_flip, crow_flip,
                    crow_flip != 0 ? xld3 - 16 : 256 - xld3, crow_flip != 0 ? yld3 - 16 : 256 - yld3,
                    Machine.visible_area, TRANSPARENCY_PEN, 0);
        }
    };
}
