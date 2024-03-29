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
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.osdependH.*;
//platform imports
import static arcadeflex.v037b7.platform.osdepend.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class cop01 {

    public static UBytePtr cop01_videoram = new UBytePtr();
    public static int[] cop01_videoram_size = new int[1];

    static UBytePtr cop01_scrollx = new UBytePtr(2);
    static int spritebank = 0;
    static int flipscreen;

    static osd_bitmap tmpbitmap2;

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr cop01_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_ptr = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_ptr++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_ptr++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                bit0 = (color_prom.read(2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_ptr++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                color_prom.inc();
            }

            color_prom.inc(2 * Machine.drv.total_colors);
            /* color_prom now points to the beginning of the lookup tables */

 /* characters use colors 0-15 */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f));
                /* ?? */
            }

            /* background tiles use colors 192-255 */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (i + 192);
            }

            /* sprites use colors 128-143 */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f) + 128);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr cop01_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((tmpbitmap2 = bitmap_alloc(2 * Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                generic_vh_stop.handler();
                return 1;
            }

            return 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopPtr cop01_vh_stop = new VhStopPtr() {
        public void handler() {
            bitmap_free(tmpbitmap2);
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr cop01_scrollx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cop01_scrollx.write(offset, data);
        }
    };

    public static WriteHandlerPtr cop01_gfxbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0 and 1 coin counters */
            coin_counter_w.handler(0, data & 1);
            coin_counter_w.handler(1, data & 2);

            /* bit 2 flip screen */
            if (flipscreen != (data & 0x04)) {
                flipscreen = data & 0x04;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* bits 4 and 5 select sprite bank */
            spritebank = ((data & 0x30) >> 4) & 0xFF;

            logerror("gfxbank = %02x\n", data);
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
    public static VhUpdatePtr cop01_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* draw the background */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 64;
                    sy = offs / 64;
                    if (flipscreen != 0) {
                        sx = 63 - sx;
                        sy = 31 - sy;
                    }

                    drawgfx(tmpbitmap2, Machine.gfx[1],
                            videoram.read(offs) + ((colorram.read(offs) & 0x03) << 8),
                            (colorram.read(offs) & 0x0c) >> 2,
                            flipscreen, flipscreen,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the background graphics */
            {
                int scrollx;

                if (flipscreen != 0) {
                    scrollx = (cop01_scrollx.read(0) + 256 * cop01_scrollx.read(1)) - 256;
                } else {
                    scrollx = -(cop01_scrollx.read(0) + 256 * cop01_scrollx.read(1));
                }

                copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scrollx}, 0, null, Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* draw the sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                int attr = spriteram.read(offs + 2);
                int numtile = spriteram.read(offs + 1);
                int flipx = attr & 4;
                int sx, sy;

                if ((numtile & 0x80) != 0) /* high tiles are bankswitched */ {
                    if ((spritebank & 1) != 0) {
                        numtile += 128;
                    } else if ((spritebank & 2) != 0) {
                        numtile += 256;
                    }
                }

                sy = 240 - spriteram.read(offs);
                sx = (spriteram.read(offs + 3) - 0x80) + 256 * (attr & 1);
                if (flipscreen != 0) {
                    sx = 240 - sx;
                    sy = 240 - sy;
                    flipx = NOT(flipx);
                }

                drawgfx(bitmap, Machine.gfx[2],
                        numtile,
                        (attr & 0xf0) >> 4,
                        flipx, flipscreen,
                        sx, sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }

            /* draw the foreground characters */
            for (offs = cop01_videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;

                sx = offs % 32;
                sy = offs / 32;
                if (flipscreen != 0) {
                    sx = 31 - sx;
                    sy = 31 - sy;
                }

                drawgfx(bitmap, Machine.gfx[0],
                        cop01_videoram.read(offs),
                        0, /* is there a color selector missing? */
                        flipscreen, flipscreen,
                        8 * sx, 8 * sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 15);
            }
        }
    };
}
