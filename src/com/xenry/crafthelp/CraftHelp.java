package com.xenry.crafthelp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * CraftHelp created by Henry Jake on December 16, 2015.
 * Copyright 2015 Henry Jake.
 * All content in this file may not be used without written consent of Henry Jake.
 */
public class CraftHelp extends JavaPlugin implements Listener {

    public void onEnable(){
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("CraftHelp enabled.");
    }

    @EventHandler
    public void on(SignChangeEvent e){
        if(!e.getLine(0).replace(" ","").equalsIgnoreCase("[Craft]")) return;
        if(Material.getMaterial(e.getLine(1).replace(" ","").toUpperCase()) == null){
            e.setLine(0, "§c[Craft]");
            e.setLine(1, "§4§lINVALID");
        }else e.setLine(0, "§a[Craft]");
    }

    @EventHandler
    public void on(PlayerInteractEvent e) {
        if(e.isCancelled()) return;
        Action a = e.getAction();
        if(e.getClickedBlock() == null) return;
        BlockState b = e.getClickedBlock().getState();
        if(!(b instanceof Sign)) return;
        Sign s = (Sign)b;
        if(!s.getLine(0).equalsIgnoreCase("§a[Craft]")) return;
        e.setCancelled(e.isCancelled() || !e.getPlayer().isSneaking());
        open(e.getPlayer(), s.getLine(1), 0);
    }

    @EventHandler
    public void on(PlayerCommandPreprocessEvent e){
        if(e.isCancelled()) return;
        String[] split = e.getMessage().substring(1).split(" ");
        if(split.length < 1) return;
        String label = split[0];
        Player p = e.getPlayer();
        String[] args = new String[split.length-1];
        for(int i = 1; i < split.length; i++)
            args[i-1] = split[i];
        if(label.equalsIgnoreCase("craft")){
            e.setCancelled(true);
            if(args.length < 1){
                p.sendMessage("§cUsage: §7/craft <item> [index]");
                return;
            }
            int index = 1;
            if(args.length > 1){
                try{
                    index = Integer.parseInt(args[1]);
                }catch(Exception ex){
                    p.sendMessage("§7" + args[1] + "§c is not a valid integer.");
                    return;
                }
            }
            if(index < 1){
                p.sendMessage("§cThe index must be at least 1.");
                return;
            }
            open(p, args[0], index-1);
            return;
        }if(label.equalsIgnoreCase("part")){
            //e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(InventoryClickEvent e){
        if(e.getClickedInventory() == null) return;
        if(e.getClickedInventory().getName() == null) return;
        e.setCancelled(e.isCancelled() || e.getClickedInventory().getName().startsWith("Recipe of "));
    }

    public void open(Player p, String itemName, int index) {
        Material mat = Material.getMaterial(itemName.toUpperCase());
        if(mat == null){
            p.sendMessage("§cInvalid block name: §7" + itemName + "§c.");
            return;
        }
        List<Recipe> recipes = Bukkit.getRecipesFor(new ItemStack(mat));
        if(recipes.size() < 1){
            p.sendMessage("§cThere are no recipes for this item.");
            return;
        }
        if(recipes.size() <= index){
            p.sendMessage("§cThere are only §e" + recipes.size() + "§c recipe(s) for this item.");
            return;
        }
        Recipe recipe = recipes.get(index);
        ShapedRecipe r;
        if(recipe instanceof ShapedRecipe) r = (ShapedRecipe)recipe;
        else if(recipe instanceof ShapelessRecipe){
            r = new ShapedRecipe(new ItemStack(mat));
            Character ch = 'a';
            String map = "         ";
            for(ItemStack is : ((ShapelessRecipe)recipe).getIngredientList()){
                r.setIngredient(ch, is.getType());
                map = map.replaceFirst(" ", (ch++).toString());
            }
            r.shape(map.substring(0, 2), map.substring(3, 5), map.substring(6, 8));
        }else if(recipe instanceof FurnaceRecipe){
            p.sendMessage("§aSmelt: §6" + ((FurnaceRecipe)recipe).getInput().getType().toString());
            return;
        }else{
            p.sendMessage("§cInvalid recipe type!");
            return;
        }
        Inventory inv = Bukkit.getServer().createInventory(p, InventoryType.WORKBENCH, "Recipe for " + mat.toString() + " »");
        int slot = 0;
        inv.setItem(slot++, r.getResult());
        for(String shape : r.getShape())
            for(char ch : shape.toCharArray())
                inv.setItem(slot++, r.getIngredientMap().get(ch));
        p.openInventory(inv);
    }

}
