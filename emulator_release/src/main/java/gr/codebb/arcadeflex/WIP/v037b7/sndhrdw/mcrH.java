/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.WIP.v037b7.sndhrdw;

import static gr.codebb.arcadeflex.WIP.v037b7.sndhrdw.mcr.*;
import static gr.codebb.arcadeflex.v037b7.mame.driverH.*;
import static gr.codebb.arcadeflex.old.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.sndintrfH.*;

public class mcrH {

    /**
     * ********** Sound Configuration **************
     */
    public static final int MCR_SSIO = 0x01;
    public static final int MCR_CHIP_SQUEAK_DELUXE = 0x02;
    public static final int MCR_SOUNDS_GOOD = 0x04;
    public static final int MCR_TURBO_CHIP_SQUEAK = 0x08;
    public static final int MCR_SQUAWK_N_TALK = 0x10;
    public static final int MCR_WILLIAMS_SOUND = 0x20;

    public static void MCR_CONFIGURE_SOUND(int x) {
        mcr_sound_config = x;
    }

    /**
     * ********** SSIO CPU and sound definitions **************
     */
    public static MachineCPU SOUND_CPU_SSIO = new MachineCPU(
            CPU_Z80 | CPU_AUDIO_CPU,
            2000000, /* 2 MHz */
            ssio_readmem, ssio_writemem, null, null,
            interrupt, 26
    );

    public static MachineSound SOUND_SSIO = new MachineSound(
            SOUND_AY8910,
            ssio_ay8910_interface
    );

    /*TODO*///
/*TODO*///
/*TODO*////************ Chip Squeak Deluxe CPU and sound definitions ***************/
/*TODO*///
/*TODO*///
/*TODO*///#define SOUND_CPU_CHIP_SQUEAK_DELUXE				\
/*TODO*///	{												\
/*TODO*///		CPU_M68000 | CPU_AUDIO_CPU,					\
/*TODO*///		15000000/2,	/* 7.5 MHz */					\
/*TODO*///		csdeluxe_readmem,csdeluxe_writemem,0,0,		\
/*TODO*///		ignore_interrupt,1							\
/*TODO*///	}
/*TODO*///
    public static MachineSound SOUND_CHIP_SQUEAK_DELUXE = new MachineSound(
            SOUND_DAC,
            mcr_dac_interface
    );
    /*TODO*///
/*TODO*///
/*TODO*////************ Sounds Good CPU and sound definitions ***************/
/*TODO*///
/*TODO*///
/*TODO*///#define SOUND_CPU_SOUNDS_GOOD						\
/*TODO*///	{												\
/*TODO*///		CPU_M68000 | CPU_AUDIO_CPU,					\
/*TODO*///		16000000/2,	/* 8.0 MHz */					\
/*TODO*///		soundsgood_readmem,soundsgood_writemem,0,0,	\
/*TODO*///		ignore_interrupt,1							\
/*TODO*///	}
/*TODO*///
/*TODO*///#define SOUND_SOUNDS_GOOD SOUND_CHIP_SQUEAK_DELUXE
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////************ Turbo Chip Squeak CPU and sound definitions ***************/
/*TODO*///
    public static MachineCPU SOUND_CPU_TURBO_CHIP_SQUEAK = new MachineCPU(
            CPU_M6809 | CPU_AUDIO_CPU,
            9000000 / 4, /* 2.25 MHz */
            turbocs_readmem, turbocs_writemem, null, null,
            ignore_interrupt, 1
    );

    public static MachineSound SOUND_TURBO_CHIP_SQUEAK = new MachineSound(
            SOUND_DAC,
            mcr_dac_interface
    );
    /*TODO*///
    /*TODO*///#define SOUND_CPU_TURBO_CHIP_SQUEAK_PLUS_SOUNDS_GOOD \
    /*TODO*///	SOUND_CPU_TURBO_CHIP_SQUEAK,					\
    /*TODO*///	SOUND_CPU_SOUNDS_GOOD
    /*TODO*///
    /*TODO*///#define SOUND_TURBO_CHIP_SQUEAK_PLUS_SOUNDS_GOOD	\
    /*TODO*///	{												\
    /*TODO*///		SOUND_DAC,									\
    /*TODO*///		&mcr_dual_dac_interface						\
    /*TODO*///	}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////************ Squawk & Talk CPU and sound definitions ***************/
    /*TODO*///
    /*TODO*///
    /*TODO*///#define SOUND_CPU_SQUAWK_N_TALK						\
    /*TODO*///	{												\
    /*TODO*///		CPU_M6802 | CPU_AUDIO_CPU,					\
    /*TODO*///		3580000/4,	/* .8 MHz */					\
    /*TODO*///		squawkntalk_readmem,squawkntalk_writemem,0,0,\
    /*TODO*///		ignore_interrupt,1							\
    /*TODO*///	}
    /*TODO*///
    /*TODO*///#define SOUND_SQUAWK_N_TALK							\
    /*TODO*///	{												\
    /*TODO*///		SOUND_TMS5220,								\
    /*TODO*///		&squawkntalk_tms5220_interface				\
    /*TODO*///	}
    /*TODO*///    

}
