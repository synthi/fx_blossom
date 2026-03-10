local fx = require("fx/lib/fx")
local mod = require("core/mods")
local hook = require("core/hook")
local tab = require("tabutil")

-- Begin post-init hack block (Estándar de inyección fx-mod)
if hook.script_post_init == nil and mod.hook.patched == nil then
    mod.hook.patched = true
    local old_register = mod.hook.register
    local post_init_hooks = {}
    mod.hook.register = function(h, name, f)
        if h == "script_post_init" then
            post_init_hooks[name] = f
        else
            old_register(h, name, f)
        end
    end
    mod.hook.register('script_pre_init', '!replace init for fake post init', function()
        local old_init = init
        init = function()
            if old_init then old_init() end -- Nil coalescing obligatorio
            for i, k in ipairs(tab.sort(post_init_hooks)) do
                local cb = post_init_hooks[k]
                print('calling: ', k)
                local ok, err = pcall(cb)
                if not ok then
                    print('hook: ' .. k .. ' failed, error: ' .. tostring(err))
                end
            end
        end
    end)
end
-- end post-init hack block

-- Declaración local absoluta (Fase 1)
local FxBlossom = fx:new{
    subpath = "/fx_blossom"
}

-- Asignación de métodos (Fase 2)
function FxBlossom:add_params()
    params:add_separator("fx_blossom", "fx blossom")
    -- Agrupamos los 7 parámetros bajo un solo menú colapsable para mantener la UI limpia
    params:add_group("fx_blossom", "fx blossom", 8)
    FxBlossom:add_slot("fx_blossom_slot", "slot")
    
    -- Tapers: id, name, key, min, max, default, k (curve), units
    FxBlossom:add_taper("fx_blossom_decay", "decay", "decay", 0.1, 100.0, 4.8, 3, "s")
    FxBlossom:add_taper("fx_blossom_bloom", "bloom", "bloom", 0.01, 2.0, 1.80, 0, "")
    FxBlossom:add_taper("fx_blossom_damp", "damp", "damp", 200, 18000, 3400, 4, "hz")
    FxBlossom:add_taper("fx_blossom_predelay", "predelay", "predelay", 0.0, 1.0, 0.2, 0, "s")
    FxBlossom:add_taper("fx_blossom_mod_rate", "mod rate", "mod_rate", 0.0, 10.0, 0.4, 0, "hz")
    FxBlossom:add_taper("fx_blossom_mod_depth", "mod depth", "mod_depth", 0.0, 0.0015, 0.0005, 0, "s")
end

mod.hook.register("script_post_init", "fx blossom mod post init", function()
    FxBlossom:add_params()
end)

mod.hook.register("script_post_cleanup", "blossom mod post cleanup", function()
    -- Reservado para recolección de basura si fuera necesario
end)

return FxBlossom
