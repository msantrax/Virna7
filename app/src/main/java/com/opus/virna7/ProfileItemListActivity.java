package com.opus.virna7;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import java.util.ArrayList;

/**
 * An activity representing a list of ProfileItems. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ProfileItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ProfileItemListActivity extends AppCompatActivity {

    public static final String TAG = "PROFEDIT";

    private SharedPreferences preferences ;
    private ProfileEntry profile;
    private Virna7Application virna;
    private ArrayList<ProfileFlatEntry> flatentries;
    private String profilename;

    private RecyclerView recyclerView;
    private ProfileFlatEntryRecyclerViewAdapter adapter;


    private int profentryicon;
    private int profphaseicon;
    private int profvalueicon;

    private ProfileItemRootDetailFragment rootfragment;
    private ProfileItemPhaseDetailFragment phasefragment;
    private ProfileItemValueDetailFragment valuefragment;

    private MenuItem newmenu;
    private MenuItem copymenu;
    private MenuItem insertmenu;
    private MenuItem appendmenu;
    private MenuItem deletemenu;
    private MenuItem savemenu;
    private MenuItem loadmenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profileitem_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Vale / Logitech - Base de Dados de Atividades" );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        registerForContextMenu(fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
//
//            }
//        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        virna  = (Virna7Application)getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        profilename = getIntent().getStringExtra("profile");

        profile = virna.getProfilemanager().cloneProfileByName(profilename);
        flatentries = profile.getFlatEntries();

        profentryicon = getResources().getIdentifier("@android:drawable/btn_star_big_on", "drawable", getPackageName());
        profphaseicon = getResources().getIdentifier("@drawable/endphase", "drawable", getPackageName());
        profvalueicon = getResources().getIdentifier("@drawable/legalmoves", "drawable", getPackageName());

        rootfragment = ProfileItemRootDetailFragment.newInstance(0) ;
        phasefragment = ProfileItemPhaseDetailFragment.newInstance(1) ;
        valuefragment = ProfileItemValueDetailFragment.newInstance(2);


        recyclerView = (RecyclerView)findViewById(R.id.profileitem_list);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_edition_gmenu, menu);
        return true;
    }

//    public void onCreateContextMenu(ContextMenu menu, View v,
//                                    ContextMenu.ContextMenuInfo menuInfo) {
    public boolean onPrepareOptionsMenu(Menu menu){

        ProfileFlatEntryRecyclerViewAdapter.ViewHolder vh = adapter.getSelectedViewHolder();

        if (copymenu == null) {
            newmenu = menu.findItem(R.id.profile_edition_menu_newaction);
            copymenu = menu.findItem(R.id.profile_edition_menu_copyaction);
            insertmenu = menu.findItem(R.id.profile_edition_menu_insertaction);
            appendmenu = menu.findItem(R.id.profile_edition_menu_appendaction);
            deletemenu = menu.findItem(R.id.profile_edition_menu_deleteaction);
            savemenu = menu.findItem(R.id.profile_edition_menu_saveaction);
            loadmenu = menu.findItem(R.id.profile_edition_menu_loadaction);
        }

        if (vh == null){
            savemenu.setEnabled(false);
            loadmenu.setEnabled(false);
            newmenu.setEnabled(false);
            copymenu.setEnabled(false);
            insertmenu.setEnabled(false);
            deletemenu.setEnabled(false);
            appendmenu.setEnabled(false);
            return true;
        }

        if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.ROOT){
            savemenu.setEnabled(true);
            loadmenu.setEnabled(true);
            newmenu.setEnabled(true);
            copymenu.setEnabled(false);
            insertmenu.setEnabled(false);
            deletemenu.setEnabled(false);
            if(virna.getProfileclipboardtype() == ProfileFlatEntry.FLATYPE.PHASE) appendmenu.setEnabled(true);
            if(virna.getProfileclipboardtype() == ProfileFlatEntry.FLATYPE.VALUE){
                insertmenu.setEnabled(false);
                appendmenu.setEnabled(false);
            }
        }
        else if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.PHASE){
            savemenu.setEnabled(false);
            loadmenu.setEnabled(false);
            newmenu.setEnabled(true);
            copymenu.setEnabled(true);
            deletemenu.setEnabled(true);
            if(virna.getProfileclipboardtype() == ProfileFlatEntry.FLATYPE.PHASE){
                appendmenu.setEnabled(true);
                insertmenu.setEnabled(true);
            }
            else if(virna.getProfileclipboardtype() == ProfileFlatEntry.FLATYPE.VALUE){
                appendmenu.setEnabled(true);
                insertmenu.setEnabled(false);
            }
            else{
                appendmenu.setEnabled(false);
                insertmenu.setEnabled(false);
            }
        }
        else if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.VALUE){
            savemenu.setEnabled(false);
            loadmenu.setEnabled(false);
            newmenu.setEnabled(false);
            copymenu.setEnabled(true);
            deletemenu.setEnabled(true);
            if(virna.getProfileclipboardtype() == ProfileFlatEntry.FLATYPE.VALUE){
                appendmenu.setEnabled(true);
            }
            else{
                appendmenu.setEnabled(false);
                insertmenu.setEnabled(false);
            }
        }
        return true;
    }


//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        ProfileFlatEntryRecyclerViewAdapter.ViewHolder vh = adapter.getSelectedViewHolder();

        if (vh == null) {
            return false;
        }
        else if (id == R.id.profile_edition_menu_newaction){
            if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.ROOT){
                // adicionar phase
                //Log.i(TAG, "Adicionando nova fase");
                ProfilePhase pp = ProfilePhase.create();
                ProfileEntry pe = vh.mItem.getRoot();
                pe.addPhase(pp);
            }
            if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.PHASE){
                // adicionar value
                Log.i(TAG, "Adicionando novo valor");
                ProfileValue pv = ProfileValue.create();
                ProfilePhase pp = vh.mItem.getPhase();
                pp.addValue(pv);
            }
            flatentries = profile.getFlatEntries();
            adapter.updateLeftPanel(flatentries);
        }
        else if (id == R.id.profile_edition_menu_copyaction){
            if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.PHASE){
                virna.setProfileclipboardtype(ProfileFlatEntry.FLATYPE.PHASE);
                virna.setProfileclipboardphase(vh.mItem.getPhase().clone());
            }
            if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.VALUE){
                virna.setProfileclipboardtype(ProfileFlatEntry.FLATYPE.VALUE);
                virna.setProfileclipboardvalue(vh.mItem.getValue().clone());
            }
        }
        else if (id == R.id.profile_edition_menu_insertaction){
            if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.PHASE && virna.getProfileclipboardtype() == ProfileFlatEntry.FLATYPE.PHASE){
                profile.pastePhase(vh.mItem.getPhase(), virna.getProfileclipboardphase(), false);
            }
            else if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.VALUE && virna.getProfileclipboardtype() == ProfileFlatEntry.FLATYPE.VALUE){
                vh.mItem.getValue().getParent().addValue(virna.getProfileclipboardvalue());
            }
            flatentries = profile.getFlatEntries();
            adapter.updateLeftPanel(flatentries);
        }
        else if (id == R.id.profile_edition_menu_appendaction){
            if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.ROOT && virna.getProfileclipboardtype() == ProfileFlatEntry.FLATYPE.PHASE){
                profile.addPhase(virna.getProfileclipboardphase());
            }
            else if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.PHASE && virna.getProfileclipboardtype() == ProfileFlatEntry.FLATYPE.PHASE){
                profile.pastePhase(vh.mItem.getPhase(), virna.getProfileclipboardphase(), true);
            }
            else if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.PHASE && virna.getProfileclipboardtype() == ProfileFlatEntry.FLATYPE.VALUE){
                vh.mItem.getPhase().addValue(virna.getProfileclipboardvalue());
            }
            else if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.VALUE && virna.getProfileclipboardtype() == ProfileFlatEntry.FLATYPE.VALUE){
                vh.mItem.getValue().getParent().addValue(virna.getProfileclipboardvalue());
            }
            flatentries = profile.getFlatEntries();
            adapter.updateLeftPanel(flatentries);
        }
        else if (id == R.id.profile_edition_menu_deleteaction){
            if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.PHASE){
                ProfilePhase pp = vh.mItem.getPhase();
                profile.removePhase(pp);
            }
            if (vh.mItem.getType() == ProfileFlatEntry.FLATYPE.VALUE){
                ProfileValue pv = vh.mItem.getValue();
                pv.getParent().removeValue(pv);
            }
            flatentries = profile.getFlatEntries();
            adapter.updateLeftPanel(flatentries);
        }
        else if (id == R.id.profile_edition_menu_saveaction){

            if (virna.getProfilemanager().hasProfile(profile.getName())){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setPositiveButton("Atualizar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        virna.getProfilemanager().setProfileByName(profile.getName(), profile);
                        virna.getProfilemanager().saveProfile();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                builder.setMessage("O perfil " + profile.getName() + " já existe na base de dados. Deseja sobre escreve-lo ?")
                        .setTitle("Gravar perfil modificado");

                AlertDialog dialog = builder.create();
                dialog.show();

            }
            else{
                virna.getProfilemanager().addProfile(profile);
                virna.getProfilemanager().saveProfile();
            }


        }
        else if (id == R.id.profile_edition_menu_loadaction){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(" O.K.", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    profile = virna.getProfilemanager().cloneProfileByName(profilename);
                    flatentries = profile.getFlatEntries();
                    adapter.updateLeftPanel(flatentries);
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            builder.setMessage("As modificações introduzidas nesse perfil serão perdidas. Deseja prosseguir ?")
                    .setTitle("Recarregar perfil");

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart(){
        super.onStart();
//        ProfileFlatEntryRecyclerViewAdapter.ViewHolder v =
//                (ProfileFlatEntryRecyclerViewAdapter.ViewHolder)recyclerView.findViewHolderForLayoutPosition(0);
//        adapter.setSelected(v,true);
        getSupportFragmentManager().beginTransaction().replace(R.id.profileitem_detail_container, rootfragment).commit();

    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new ProfileFlatEntryRecyclerViewAdapter(flatentries);
        recyclerView.setAdapter(adapter);
    }


    public void updateLeftList(String value){
        adapter.selectedview.mContentView.setText(value);
    }


    // =========================
    public class ProfileFlatEntryRecyclerViewAdapter extends RecyclerView.Adapter<ProfileFlatEntryRecyclerViewAdapter.ViewHolder> {

        private ArrayList<ProfileFlatEntry> mValues;
        private ViewHolder selectedview;


        public ProfileFlatEntryRecyclerViewAdapter(ArrayList<ProfileFlatEntry> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profileitem_list_content, parent, false);
            return new ViewHolder(view);
        }

        public void setSelected(ProfileFlatEntryRecyclerViewAdapter.ViewHolder v, boolean select){
            if (selectedview != null) selectedview.setSelect(false);
            v.setSelect(true);
            selectedview = v;
        }

        public ProfileFlatEntryRecyclerViewAdapter.ViewHolder getSelectedViewHolder() {return selectedview;}

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            holder.mItem = mValues.get(position);

            //registerForContextMenu( holder.mContentView);

            if (holder.mItem.getType() == ProfileFlatEntry.FLATYPE.ROOT) {
                holder.mIdView.setImageResource(profentryicon);
                holder.mContentView.setText(mValues.get(position).getName());
            }
            else if (holder.mItem.getType() == ProfileFlatEntry.FLATYPE.PHASE) {
                holder.mIdView.setImageResource(profphaseicon);
                holder.mContentView.setText("   " + mValues.get(position).getName());
            }
            else if (holder.mItem.getType() == ProfileFlatEntry.FLATYPE.VALUE) {
                holder.mIdView.setImageResource(profvalueicon);
                holder.mContentView.setText("      " + mValues.get(position).getName());
            }
             else {
                holder.mContentView.setText("Entrada Não determinada");
            }


            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Bundle arguments = new Bundle();
//                    arguments.putString(ProfileItemDetailFragment.ARG_ITEM_ID, holder.mItem.getName());
//                    ProfileItemDetailFragment fragment = new ProfileItemDetailFragment();
//                    fragment.setArguments(arguments);

                    setSelected (holder, true);

                    ProfileFlatEntry.FLATYPE ptype = holder.mItem.getType();
                    if(ptype == ProfileFlatEntry.FLATYPE.ROOT ){
                        rootfragment.setEntry(holder.mItem.getRoot());
                        getSupportFragmentManager().beginTransaction().replace(R.id.profileitem_detail_container, rootfragment).commit();
                    }
                    else if(ptype == ProfileFlatEntry.FLATYPE.PHASE){
                        phasefragment.setPhase(holder.mItem.getPhase());
                        getSupportFragmentManager().beginTransaction().replace(R.id.profileitem_detail_container, phasefragment).commit();
                    }
                    else if(ptype == ProfileFlatEntry.FLATYPE.VALUE){
                        valuefragment.setValue(holder.mItem.getValue());
                        getSupportFragmentManager().beginTransaction().replace(R.id.profileitem_detail_container, valuefragment).commit();
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }


        public void updateLeftPanel(ArrayList<ProfileFlatEntry> items){
            mValues = items;
            notifyDataSetChanged();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mIdView;
            public final TextView mContentView;
            public ProfileFlatEntry mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (ImageView) view.findViewById(R.id.proflist_icon);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            public void setSelect(boolean select){
                if (select){
                    mContentView.setTextColor(0xFFFF4081);
                }
                else{
                    mContentView.setTextColor(0xFF000000);
                }
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
