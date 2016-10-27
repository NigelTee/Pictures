package materialtest.example.user.pictures;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

/**
 * RecyclerViewHolder
 * Created by Nigel.
 */

class RecyclerViewHolder extends RecyclerView.ViewHolder {

    ImageView origianalPhoto;
    private List<Image> imageList;

    RecyclerViewHolder(View itemView, List<Image> list, final Context context) {

        super(itemView);
        this.imageList = list;
        origianalPhoto = (ImageView) itemView;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getLayoutPosition();
                Image item = imageList.get(position);

                //Start SingleViewActivity class
                Intent intent = new Intent(context, SingleViewActivity.class);
                intent.putExtra("URL", item.getURL());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.getApplicationContext().startActivity(intent);
            }
        });
    }
}
