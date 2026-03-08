FxBlossom : FxBase {

    *new { 
        var ret = super.newCopyArgs(nil, \none, (
            decay: 3.0,
            bloom: 0.5,
            damp: 10000,
            predelay: 0.0,
            mod_rate: 0.5,
            mod_depth: 0.001
        ), nil, 1.0);
        ^ret;
    }

    *initClass {
        FxSetup.register(this.new);
    }

    subPath {
        ^"/fx_blossom";
    }  

    symbol {
        ^\fxBlossom;
    }

    addSynthdefs {
        SynthDef(\fxBlossom, { |inBus, outBus|
            // 1. DECLARACIÓN ABSOLUTA DE VARIABLES (Fase 1)
            var input, rev_in;
            var lfo_l, lfo_r;
            var combs_l, combs_r;
            var cross_l, cross_r;
            var ap_l, ap_r;
            var rev_filt_l, rev_filt_r;
            var rev_out_l, rev_out_r;
            var prime_combs_l, prime_combs_r;
            var prime_ap_l, prime_ap_r;
            var decay_kr, bloom_kr, damp_kr, predelay_kr, mod_rate_kr, mod_depth_kr;

            // 2. ASIGNACIÓN Y OPERACIÓN (Fase 2)
            prime_combs_l =[0.031229, 0.037270, 0.043979, 0.050354, 0.057270, 0.064770];
            prime_combs_r =[0.031479, 0.037729, 0.044354, 0.050479, 0.057354, 0.064979];
            prime_ap_l =[0.011270, 0.031729];
            prime_ap_r =[0.011604, 0.031895];

            decay_kr = \decay.kr(3.0).lag(0.1);
            bloom_kr = \bloom.kr(0.5).lag(0.1);
            damp_kr = \damp.kr(10000).lag(0.1);
            predelay_kr = \predelay.kr(0.0).lag(0.1);
            mod_rate_kr = \mod_rate.kr(0.5).lag(0.1);
            mod_depth_kr = \mod_depth.kr(0.001).lag(0.1);

            input = In.ar(inBus, 2);

            // Pre-Delay (Max 1.0s)
            rev_in = DelayN.ar(input, 1.0, predelay_kr);

            // Modulation LFOs (Descorrelación Áurea 1.1618)
            lfo_l = LFNoise2.kr(mod_rate_kr) * mod_depth_kr;
            lfo_r = LFNoise2.kr(mod_rate_kr * 1.1618) * mod_depth_kr;

            // Parallel Comb Filters (The Tank)
            combs_l = prime_combs_l.collect { |time|
                var local_time = time; // Protección estricta de scope
                CombC.ar(rev_in[0], 0.1, local_time + lfo_l, decay_kr);
            }.sum;

            combs_r = prime_combs_r.collect { |time|
                var local_time = time; // Protección estricta de scope
                CombC.ar(rev_in[1], 0.1, local_time + lfo_r, decay_kr);
            }.sum;

            // Cross-Pollination (Inyección Estéreo Cruzada del 20%)
            cross_l = combs_l + (combs_r * 0.2);
            cross_r = combs_r + (combs_l * 0.2);

            // Series Allpass Filters (Diffusion / Bloom)
            ap_l = cross_l;
            ap_r = cross_r;
            
            2.do { |i|
                var local_i = i; // Protección estricta de scope
                ap_l = AllpassN.ar(ap_l, 0.05, prime_ap_l[local_i], bloom_kr * 2.0);
                ap_r = AllpassN.ar(ap_r, 0.05, prime_ap_r[local_i], bloom_kr * 2.0);
            };

            // Post-Damping (Techo acústico global)
            rev_filt_l = LPF.ar(ap_l, damp_kr);
            rev_filt_r = LPF.ar(ap_r, damp_kr);

            // DC Blocking & Transparent Saturation (Soft-Clipper)
            rev_out_l = (LeakDC.ar(rev_filt_l) * 0.3).tanh;
            rev_out_r = (LeakDC.ar(rev_filt_r) * 0.3).tanh;

            Out.ar(outBus,[rev_out_l, rev_out_r]);
        }).add;
    }

}
