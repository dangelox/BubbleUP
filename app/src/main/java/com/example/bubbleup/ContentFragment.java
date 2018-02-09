package com.example.bubbleup;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    View myView;

    LayoutInflater myInflater;

    public ContentFragment() {
        // Required empty public constructor
    }

    public void sendToFragment(List<BubbleMarker> bubbleList, LatLngBounds bounds){
        //ListView myList = (ListView) myView.findViewById(R.id.scroll_view);
        //myList.setAdapter(new ArrayAdapter(getContext(),R.layout.fragment_scroll_view));
        LinearLayout myList = (LinearLayout) myView.findViewById(R.id.linear_view);

        for (BubbleMarker currentBubble : bubbleList) {
            if(bounds.contains(currentBubble.bubbleMarker.getPosition())){
                Log.d("BubbleUp_Fragment",currentBubble.msg);
                View container = myInflater.inflate(R.layout.fragment_post_container, myList, false);
                TextView text = (TextView) container.findViewById(R.id.textView);
                text.setText(currentBubble.msg);
                String userName = currentBubble.bubbleMarkerOption.getTitle().substring(0, Math.min(currentBubble.bubbleMarkerOption.getTitle().length(), 6));
                ImageButton userImage = (ImageButton) container.findViewById(R.id.imageButton);
                switch(userName){
                    case "User#1": userImage.setColorFilter(Color.parseColor("#ff9555"));;//setBackgroundColor(Color.parseColor("#ff9555"));
                        break;
                    case "User#2": userImage.setColorFilter(Color.parseColor("#9044D3"));//setBackgroundColor(Color.parseColor("#9044D3"));
                        break;
                    case "User#9": userImage.setColorFilter(Color.parseColor("#EA2F7E"));//setBackgroundColor(Color.parseColor("#EA2F7E"));
                        break;
                    default: userImage.setColorFilter(Color.parseColor("#28E1D3"));//setBackgroundColor(Color.parseColor("#28E1D3"));
                        break;
                }
                myList.addView(container);
            }
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContentFragment newInstance(String param1, String param2) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myInflater = inflater;
        myView = inflater.inflate(R.layout.bubble_data, container, false);

        return myView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
