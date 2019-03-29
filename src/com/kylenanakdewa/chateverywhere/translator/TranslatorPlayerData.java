package com.kylenanakdewa.chateverywhere.translator;

import com.kylenanakdewa.core.common.savedata.PlayerSaveDataSection;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

/**
 * TranslatorPlayerData
 */
class TranslatorPlayerData extends PlayerSaveDataSection {

    TranslatorPlayerData(OfflinePlayer player, Plugin plugin){
        super(player, plugin);
    }

    String getLanguageCode(){
        return data.getString("language", "en");
    }

    void setLanguageCode(String languageCode){
        data.set("language", languageCode);
    }

}