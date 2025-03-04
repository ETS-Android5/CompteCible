package com.herault.comptecible;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class ListRound extends ArrayAdapter<Resultat_archer> {
    private final Context _context;

    public ListRound(Context context, ArrayList<Resultat_archer> AResultat) {
        super(context, R.layout.resultat, AResultat);
        _context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Resultat_archer resultat_archer = getItem(position);
        final ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.resultat, parent, false);

            viewHolder.viewName = convertView.findViewById(R.id.name_resultat);
            viewHolder.score = convertView.findViewById(R.id.score);
            viewHolder.dummy = convertView.findViewById(R.id.Dummy);
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.viewName.setText(resultat_archer.getName());
        viewHolder.score.setText(Long.toString(resultat_archer.getValue()));

        viewHolder.dummy.setText(resultat_archer.getInformation());
        if ((position & 1) != 1) {
            viewHolder.viewName.setBackgroundColor(0xFFC5BFA8);
            viewHolder.score.setBackgroundColor(0xFFC5BFA8);
            viewHolder.dummy.setBackgroundColor(0xFFC5BFA8);
        } else {
            viewHolder.viewName.setBackgroundColor(Color.WHITE);
            viewHolder.score.setBackgroundColor(Color.WHITE);
            viewHolder.dummy.setBackgroundColor(Color.WHITE);
        }

     /*  viewHolder.viewName.setOnClickListener(new View.OnClickListener() {

            @Override

         public void onClick(View view) {


                //int position = (Integer) view.get();

             ViewHolder   viewHolder = (ViewHolder) view.getTag();
                 viewHolder.viewName.setBackgroundColor(Color.BLACK);
                // Access the row position here to get the correct data item

          //      Resultat_archer resultat_archer = getItem(position);

                // Do what you want here...
            }
        });*/
        return convertView;
    }

    private static class ViewHolder {
        TextView viewName;
        TextView score;
        TextView dummy;
    }

}

