package gr.codebb.arcadeflex.WIP.v037b7.sound;

import static gr.codebb.arcadeflex.v056.mame.timer.*;


/**
 * *******************************************************
 *
 * Konami 053260 PCM/ADPCM Sound Chip
 *
 ********************************************************
 */

public class k053260H {


    public static class K053260_interface {

        public K053260_interface(int clock, int region, int[] mixing_level, timer_callback irq) {
            this.clock = clock;
            this.region = region;
            this.mixing_level = mixing_level;
            this.irq = irq;
        }
        int clock;					/* clock */

        int region;					/* memory region of sample ROM(s) */

        int[] mixing_level;		/* volume */

        timer_callback irq;	/* called on SH1 complete cycle ( clock / 32 ) */

    };
}
