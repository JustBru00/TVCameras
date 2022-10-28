package com.nido.camera;

import co.aikar.idb.DB;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

public class cameraListener implements Listener {
    Camera plugin = Camera.getInstance();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        CamPlayer camPlayer = plugin.getPlayer(p);
        //check if player is inside a boat
        if(p.isInsideVehicle() && p.getVehicle() instanceof Boat) {
            //check if player is followed

            if(!camPlayer.getFollowers().isEmpty()) {
                Track track = Utils.getClosestTrack(p);
                for (Cam camera : plugin.getCameras()) {
                    if (camera.getTrack() == track) {
                        if (camera.isInsideRegion(p)) {
                            camPlayer.setBestCam(camera);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){

        Player p = e.getPlayer();
        plugin.onQuit(p);

    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){

        Player p = e.getPlayer();
        try {
            var playerRow = DB.getFirstRow("SELECT * FROM Camera_Players WHERE UUID = '" + p.getUniqueId() + "';");
            if(playerRow != null) {
                plugin.addCamPlayer(p, playerRow);
            } else {plugin.newCamPlayer(p);}
        } catch (SQLException s) {s.printStackTrace();}



    }
    @EventHandler
    public void rClickBlock(PlayerInteractEvent e) {
        CamPlayer player = plugin.getPlayer(e.getPlayer());
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (Objects.requireNonNull(e.getHand()).toString().equals("OFF_HAND")) {
            return;
        }
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }
        if(player.isEditing() && Objects.requireNonNull(e.getPlayer().getInventory().getItem(EquipmentSlot.HAND)).getType() == Material.STICK){
            player.setSelection(2, block.getLocation());
            e.getPlayer().sendMessage(ChatColor.AQUA + "Set position 2");
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void lClickBlock(PlayerInteractEvent e) {
        CamPlayer player = plugin.getPlayer(e.getPlayer());
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }
        if(player.isEditing() && Objects.requireNonNull(e.getPlayer().getInventory().getItem(EquipmentSlot.HAND)).getType() == Material.STICK){
            player.setSelection(1, block.getLocation());
            e.getPlayer().sendMessage(ChatColor.AQUA + "Set position 1");
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void lClickBlock(BlockBreakEvent e) {
        CamPlayer player = plugin.getPlayer(e.getPlayer());
        if(player.isEditing() && Objects.requireNonNull(e.getPlayer().getInventory().getItem(EquipmentSlot.HAND)).getType() == Material.STICK){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onLClick(InventoryClickEvent e){
        Player p = (Player) e.getWhoClicked();
        CamPlayer camPlayer = plugin.getPlayer(p);
        HashMap<Integer, Cam> cameraItems = camPlayer.getCameraItems();
        if (camPlayer.isInv()){
            e.setCancelled(true);
            if (e.getClick() == ClickType.LEFT){
                if (cameraItems.containsKey(e.getSlot())) {
                    Cam camera = cameraItems.get(e.getSlot());
                    camera.tpPlayer(p);
                    e.getInventory().close();
                }
            }
        }
    }
    // --NEW--
    @EventHandler
    public void onRClick(InventoryClickEvent e){

        Player p = (Player) e.getWhoClicked();
        CamPlayer camPlayer = plugin.getPlayer(p);
        HashMap<Integer, Cam> cameraItems = camPlayer.getCameraItems();
        if (camPlayer.isInv()){
            e.setCancelled(true);
            if (e.getClick() == ClickType.RIGHT){
                if (cameraItems.containsKey(e.getSlot())) {
                    Cam camera = cameraItems.get(e.getSlot());
                    if (camPlayer.isCameraDisabled(camera.getId())){
                        camPlayer.enableCamera(camera.getId());
                    }else {
                        camPlayer.disableCamera(camera.getId());
                    }
                    e.getInventory().close();
                    Utils.openMenu(p);
                }
            }
        }
    }
    @EventHandler
    public void onDrag(InventoryDragEvent e){
        Player p = (Player) e.getWhoClicked();
        CamPlayer camPlayer = plugin.getPlayer(p);
        if (camPlayer.isInv()){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Player p = (Player) e.getPlayer();
        CamPlayer camPlayer = plugin.getPlayer(p);
        if(e.getInventory().getType() == InventoryType.CHEST) {
            if (camPlayer.isInv()) {
                camPlayer.setInv(false);
            }
        }
    }
}
