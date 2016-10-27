package materialtest.example.user.pictures;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * RecyclerViewAdapter
 * Created by Nigel.
 */

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    private static int mImageWidth;
    private static int mImageHeight;
    private List<Image> itemList;
    private Context context;

    RecyclerViewAdapter(Context context, List<Image> itemList, int mImageHeight, int mImageWidth) {
        this.itemList = itemList;
        this.context = context;
        RecyclerViewAdapter.mImageWidth = mImageWidth;
        RecyclerViewAdapter.mImageHeight = mImageHeight;
    }

    //Create resized view instead of using xml
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mImageWidth, mImageHeight);
        imageView.setLayoutParams(params);
        return new RecyclerViewHolder(imageView, itemList, context);
    }

    //Use Picasso to load image
    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {

        Picasso.with(context)
                .load(itemList.get(position).getURL())
                .resize(mImageWidth, mImageHeight)
                .placeholder(R.drawable.ic_photo_grey600_48dp)
                .into(holder.origianalPhoto);

        //holder.origianalPhoto.setImageBitmap(itemList.get(position).getBmp());
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}
