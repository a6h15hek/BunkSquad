package com.socialapp.antariksh.bunksquad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;


public class BunksquadMainActivity extends AppCompatActivity {
    BottomNavigationView mainBottomNavigation;
    Fragment fragmentToOpen=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bunksquad_main);
        mainBottomNavigation=findViewById(R.id.mainScreenBottomNavigation);
        mainBottomNavigation.setOnNavigationItemSelectedListener(menuItemClickListener);

        //set intent fragment to set the fragment;
        String menuToOpen=getIntent().getStringExtra("FRAGMENT");
        if (menuToOpen != null&&menuToOpen.equals("AM")) {
            fragmentToOpen=new AttendenceManagerFragment();
            mainBottomNavigation.setSelectedItemId(R.id.AttendenceManagerMenuItem);
        }else if(menuToOpen != null&&menuToOpen.equals("MB")){
            fragmentToOpen=new VotingFragment(null);
            mainBottomNavigation.setSelectedItemId(R.id.SocialMassBunkMenuItem);
        }else {
            fragmentToOpen=new CalculatorFragment();
        }
        Uri groupJoiningLink=getIntent().getData();
        if(groupJoiningLink!=null){
            List<String> params=groupJoiningLink.getPathSegments();
            if(params.size()>0){
                fragmentToOpen=new VotingFragment(params.get(0));
            }
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.mainScreenContent,
                fragmentToOpen).commit();

    }

    MenuItem previousSelectedItem=null;
    Fragment selectedFragment=null;
    private BottomNavigationView.OnNavigationItemSelectedListener menuItemClickListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.BunkSquadCalculatorMenuItem:
                            if(previousSelectedItem == item){
                                break;
                            }
                            selectedFragment=new CalculatorFragment();
                            break;
                        case R.id.AttendenceManagerMenuItem:
                            if(previousSelectedItem == item){
                                break;
                            }
                            selectedFragment=new AttendenceManagerFragment();
                            break;
                        case R.id.SocialMassBunkMenuItem:
                            if(previousSelectedItem == item){
                                break;
                            }
                            selectedFragment=new VotingFragment(null);
                            break;
                        default:
                            selectedFragment=new CalculatorFragment();
                            break;
                    }
                    previousSelectedItem = item;
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainScreenContent,
                            selectedFragment).commit();
                    return true;
                }
            };

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
