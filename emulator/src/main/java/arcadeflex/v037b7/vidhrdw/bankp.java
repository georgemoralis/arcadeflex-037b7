/*
 * ported to v0.37b7
 *
 */
package arcadeflex.v037b7.vidhrdw;

//common imports
import static arcadeflex.common.ptrLib.*;
import static arcadeflex.common.libc.cstring.*;
import static arcadeflex.common.libc.expressions.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.mame.*;
import static arcadeflex.v037b7.mame.drawgfx.*;
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class bankp {

    public static UBytePtr bankp_videoram2 = new UBytePtr();
    public static UBytePtr bankp_colorram2 = new UBytePtr();
    static char[] dirtybuffer2;
    static osd_bitmap tmpbitmap2;
    static int scroll_x;
    static int flipscreen;
    static int priority;

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }

    public static VhConvertColorPromPtr bankp_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                color_prom.inc();
            }

            /* color_prom now points to the beginning of the lookup table */
 /* charset #1 lookup table */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) ((color_prom.readinc()) & 0x0f);
            }

            color_prom.inc(128);
            /* skip the bottom half of the PROM - seems to be not used */

 /* charset #2 lookup table */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) ((color_prom.readinc()) & 0x0f);
            }

            /* the bottom half of the PROM seems to be not used */
        }
    };

    /**
     * *************************************************************************
     * <p>
     * Start the video hardware emulation.
     * <p>
     * *************************************************************************
     */
    public static VhStartPtr bankp_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[videoram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, videoram_size[0]);

            if ((tmpbitmap2 = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;
            }

            return 0;
        }
    };

    public static WriteHandlerPtr bankp_scroll_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scroll_x = data;
        }
    };

    /**
     * *************************************************************************
     * <p>
     * Stop the video hardware emulation.
     * <p>
     * *************************************************************************
     */
    public static VhStopPtr bankp_vh_stop = new VhStopPtr() {
        public void handler() {
            dirtybuffer2 = null;
            bitmap_free(tmpbitmap2);
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr bankp_videoram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (bankp_videoram2.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                bankp_videoram2.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr bankp_colorram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (bankp_colorram2.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                bankp_colorram2.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr bankp_out_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0-1 are playfield priority */
 /* TODO: understand how this works, currently the only thing I do is */
 /* invert the layer order when priority == 2 */
            priority = data & 0x03;

            /* bits 2-3 unknown (2 is used) */
 /* bit 4 controls NMI */
            if ((data & 0x10) != 0) {
                interrupt_enable_w.handler(0, 1);
            } else {
                interrupt_enable_w.handler(0, 0);
            }

            /* bit 5 controls screen flip */
            if ((data & 0x20) != flipscreen) {
                flipscreen = data & 0x20;
                memset(dirtybuffer, 1, videoram_size[0]);
                memset(dirtybuffer2, 1, videoram_size[0]);
            }

            /* bits 6-7 unknown */
        }
    };

    /**
     * *************************************************************************
     * <p>
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     * <p>
     * *************************************************************************
     */
    public static VhUpdatePtr bankp_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, flipx;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    flipx = colorram.read(offs) & 0x04;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + 256 * ((colorram.read(offs) & 3) >> 0),
                            colorram.read(offs) >> 3,
                            flipx, flipscreen,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }

                if (dirtybuffer2[offs] != 0) {
                    int sx, sy, flipx;

                    dirtybuffer2[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    flipx = bankp_colorram2.read(offs) & 0x08;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                    }

                    drawgfx(tmpbitmap2, Machine.gfx[1],
                            bankp_videoram2.read(offs) + 256 * (bankp_colorram2.read(offs) & 0x07),
                            bankp_colorram2.read(offs) >> 4,
                            flipx, flipscreen,
                            8 * sx, 8 * sy,
                            Machine.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmaps to the screen */
            {
                int scroll;

                scroll = -scroll_x;

                if (priority == 2) {
                    copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scroll}, 0, null, Machine.visible_area, TRANSPARENCY_NONE, 0);
                    copybitmap(bitmap, tmpbitmap2, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_COLOR, 0);
                } else {
                    copybitmap(bitmap, tmpbitmap2, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
                    copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scroll}, 0, null, Machine.visible_area, TRANSPARENCY_COLOR, 0);
                }
            }
        }
    };
}
