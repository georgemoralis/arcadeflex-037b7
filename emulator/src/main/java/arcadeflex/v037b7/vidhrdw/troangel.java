/*
 * ported to 0.37b7
 * ported to v0.36
 *
 */
package arcadeflex.v037b7.vidhrdw;

//common imports
import static arcadeflex.common.ptrLib.*;
import static arcadeflex.common.libc.expressions.*;
import static arcadeflex.common.libc.cstring.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.mame.*;
import static arcadeflex.v037b7.mame.drawgfx.*;
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.inptport.*;
import static arcadeflex.v037b7.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.generic.*;
//to be organized
import static gr.codebb.arcadeflex.old.mame.drawgfx.drawgfx;
import static gr.codebb.arcadeflex.WIP.v037b7.mame.mame.Machine;
import static gr.codebb.arcadeflex.WIP.v037b7.mame.drawgfx.copyscrollbitmap;

public class troangel {

    public static UBytePtr troangel_scroll = new UBytePtr();

    public static int flipscreen;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Tropical Angel has two 256x4 character palette PROMs, one 32x8 sprite
     * palette PROM, and one 256x4 sprite color lookup table PROM.
     *
     * I don't know for sure how the palette PROMs are connected to the RGB
     * output, but it's probably something like this; note that RED and BLUE are
     * swapped wrt the usual configuration.
     *
     * bit 7 -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED -- 220 ohm
     * resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE bit 0 -- 1
     * kohm resistor -- BLUE
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr troangel_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            int p_inc = 0;
            /* character palette */
            for (i = 0; i < 256; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = 0;
                bit1 = (color_prom.read(256) >> 2) & 0x01;
                bit2 = (color_prom.read(256) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read(0) >> 3) & 0x01;
                bit1 = (color_prom.read(256) >> 0) & 0x01;
                bit2 = (color_prom.read(256) >> 1) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) i;
                color_prom.inc();
            }

            color_prom.inc(256);
            /* color_prom now points to the beginning of the sprite palette */

 /* sprite palette */
            for (i = 0; i < 16; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                color_prom.inc();
            }

            color_prom.inc(16);
            /* color_prom now points to the beginning of the sprite lookup table */

 /* sprite lookup table */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (256 + (~color_prom.read() & 0x0f));
                color_prom.inc();
            }
        }
    };

    public static WriteHandlerPtr troangel_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* screen flip is handled both by software and hardware */
            data ^= ~readinputport(4) & 1;

            if (flipscreen != (data & 1)) {
                flipscreen = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            coin_counter_w.handler(0, data & 0x02);
            coin_counter_w.handler(1, data & 0x20);
        }
    };

    static void draw_background(osd_bitmap bitmap) {
        int offs;
        GfxElement gfx = Machine.gfx[0];

        for (offs = videoram_size[0] - 2; offs >= 0; offs -= 2) {
            if (dirtybuffer[offs] != 0 || dirtybuffer[offs + 1] != 0) {
                int sx, sy, code, attr, flipx;

                dirtybuffer[offs] = dirtybuffer[offs + 1] = 0;

                sx = (offs / 2) % 32;
                sy = (offs / 2) / 32;

                attr = videoram.read(offs);
                code = videoram.read(offs + 1) + ((attr & 0xc0) << 2);
                flipx = attr & 0x20;

                if (flipscreen != 0) {
                    sx = 31 - sx;
                    sy = 31 - sy;
                    flipx = NOT(flipx);
                }

                drawgfx(tmpbitmap, gfx,
                        code,
                        attr & 0x1f,
                        flipx, flipscreen,
                        8 * sx, 8 * sy,
                        null, TRANSPARENCY_NONE, 0);
            }
        }

        {
            int[] xscroll = new int[256];

            if (flipscreen != 0) {
                /* fixed */
                for (offs = 0; offs < 64; offs++) {
                    xscroll[255 - offs] = 0;
                }

                /* scroll (wraps around) */
                for (offs = 64; offs < 128; offs++) {
                    xscroll[255 - offs] = troangel_scroll.read(64);
                }

                /* linescroll (no wrap) */
                for (offs = 128; offs < 256; offs++) {
                    xscroll[255 - offs] = troangel_scroll.read(offs);
                }
            } else {
                /* fixed */
                for (offs = 0; offs < 64; offs++) {
                    xscroll[offs] = 0;
                }

                /* scroll (wraps around) */
                for (offs = 64; offs < 128; offs++) {
                    xscroll[offs] = -troangel_scroll.read(64);
                }

                /* linescroll (no wrap) */
                for (offs = 128; offs < 256; offs++) {
                    xscroll[offs] = -troangel_scroll.read(offs);
                }
            }

            copyscrollbitmap(bitmap, tmpbitmap, 256, xscroll, 0, null, Machine.visible_area, TRANSPARENCY_NONE, 0);
        }
    }

    static void draw_sprites(osd_bitmap bitmap) {
        int offs;

        for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
            /*unsigned*/ char attributes = (char) (spriteram.read(offs + 1) & 0xFF);
            int sx = spriteram.read(offs + 3);
            int sy = ((224 - spriteram.read(offs + 0) - 32) & 0xff) + 32;
            int code = spriteram.read(offs + 2);
            int color = attributes & 0x1f;
            int flipy = attributes & 0x80;
            int flipx = attributes & 0x40;

            int tile_number = code & 0x3f;

            int bank = 0;
            if ((code & 0x80) != 0) {
                bank += 1;
            }
            if ((attributes & 0x20) != 0) {
                bank += 2;
            }

            if (flipscreen != 0) {
                sx = 240 - sx;
                sy = 224 - sy;
                flipx = NOT(flipx);
                flipy = NOT(flipy);
            }

            drawgfx(bitmap, Machine.gfx[1 + bank],
                    tile_number,
                    color,
                    flipx, flipy,
                    sx, sy,
                    Machine.visible_area, TRANSPARENCY_PEN, 0);
        }
    }

    public static VhUpdatePtr troangel_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            draw_background(bitmap);
            draw_sprites(bitmap);
        }
    };
}
