package com.example.chatw;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class AcesoTabsAdapter extends FragmentPagerAdapter {
    public AcesoTabsAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 1:
                GruposFragment gruposFragment = new GruposFragment();
                return gruposFragment;
            case 2:
                ContactosFragment contactosFragment = new ContactosFragment();
                return contactosFragment;
            case 3:
                SolicitudesFragment solicitudesFragment = new SolicitudesFragment();
                return solicitudesFragment;
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return 4;
    }
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chat";
            case 1:
                return "Grupo";
            case 2:
                return "Contacto";
            case 3:
                return "Solicitudes";
            default:
                return null;
        }
    }
}
