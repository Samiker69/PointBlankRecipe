ServerEvents.recipes(event => {
    const partsToRemove = [
        'pointblank_recipe:triggermechanism',
        'pointblank_recipe:frameoftherifle',
        'pointblank_recipe:frameofsniperifle',
        'pointblank_recipe:frameofsubmachinegun',
        'pointblank_recipe:frameofshotgun',
        'pointblank_recipe:gunmagazine',
        'pointblank_recipe:magazineofsubmachinegun',
        'pointblank_recipe:thebarreloftherifle',
        'pointblank_recipe:buttform4a1type'
    ];

    partsToRemove.forEach(part => {
        event.remove({ output: part });
    });

    event.recipes.create.mechanical_crafting('pointblank_recipe:triggermechanism', [
        '  A  ',
        ' BCB ',
        '  D  '
    ], {
        A: 'mekanism:basic_control_circuit',
        B: 'create:precision_mechanism',
        C: 'minecraft:iron_block',
        D: 'create:shaft'
    });

    event.shaped('pointblank_recipe:frameoftherifle', [
        'ABA',
        'CDC',
        'ABA'
    ], {
        A: 'create:andesite_alloy',
        B: 'mekanism:ingot_steel',
        C: 'create:iron_sheet',
        D: 'create:brass_casing'
    });

    event.shaped('pointblank_recipe:gunmagazine', [
        ' A ',
        'ABA',
        ' A '
    ], {
        A: 'create:iron_sheet',
        B: 'create:andesite_alloy'
    });

    event.recipes.create.pressing('pointblank_recipe:thebarreloftherifle', [
        'mekanism:ingot_steel'
    ]);

    event.remove({ output: 'pointblank:ak47' });
    
    event.recipes.create.mechanical_crafting('pointblank:ak47', [
        '  A  ',
        ' BCD ',
        '  EF '
    ], {
        A: 'pointblank_recipe:thebarreloftherifle',
        B: 'minecraft:iron_nugget',
        C: 'pointblank_recipe:frameoftherifle',
        D: 'pointblank_recipe:gunmagazine',
        E: 'pointblank_recipe:gunmagazine',
        F: 'pointblank_recipe:triggermechanism'
    });

    event.recipes.create.crushing([
        '9x pointblank_recipe:coppernugget'
    ], 'minecraft:copper_ingot');

    console.info('PointBlank Tech Recipes Loaded Successfully');
});