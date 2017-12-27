package com.example.mikki.navigation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.CharSequence;


import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private View v;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int READ_REQUEST_CODE = 2;

    private StorageReference mStorage;
    private ProgressDialog mProgress;
    public ImageView mimageView;



    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_profile, container, false);
        configureImageView();
        return v;

    }


    public void configureImageView() {

        mStorage = FirebaseStorage.getInstance().getReference();
        mProgress = new ProgressDialog(getContext());

        mimageView = (ImageView) v.findViewById(R.id.profilePic);


        if ("defaultPic".equals(mimageView.getTag())) {

            mimageView.setOnClickListener(new View.OnClickListener() {

                public void onClick(final View v) {

                    final CharSequence profilePicture[] = new CharSequence[]{"View Profile Picture", "Take a Photo",
                            "Choose from Gallery"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setItems(profilePicture, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (profilePicture[which].equals("View Profile Picture")) {
                                mimageView.getDrawable();

                            }
                            if (profilePicture[which].equals("Take a Photo")) {
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                            if (profilePicture[which].equals("Choose from Gallery")) {
                                Intent choosePhotoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                choosePhotoIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                choosePhotoIntent.setType("image/*");
                                startActivityForResult(choosePhotoIntent, READ_REQUEST_CODE);
                            }
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        } else {

            mimageView.setOnClickListener(new View.OnClickListener() {

                public void onClick(final View v) {

                    final CharSequence profilePicture[] = new CharSequence[]{"View Profile Picture", "Take a Photo",
                            "Choose from Gallery", "Remove Profile Picture"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setItems(profilePicture, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (profilePicture[which].equals("View Profile Picture")) {

                            }
                            if (profilePicture[which].equals("Take a Photo")) {
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                            if (profilePicture[which].equals("Choose from Gallery")) {
                                Intent choosePhotoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                choosePhotoIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                choosePhotoIntent.setType("image/*");
                                startActivityForResult(choosePhotoIntent, READ_REQUEST_CODE);
                            }
                            if (profilePicture[which].equals("Remove Profile Picture")) {
                                mimageView.setImageResource(R.drawable.round_profile);
                            }
                        }

                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                final CropImage.ActivityResult result = CropImage.getActivityResult(data);
                final Uri resultUri = result.getUri();

                if (resultCode == RESULT_OK) {
                    mProgress.setMessage("Uploading Image...");
                    mProgress.show();

                    StorageReference filepath = mStorage.child("Photos").child(resultUri.getLastPathSegment());
                    filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgress.dismiss();
                            mimageView.setImageURI(resultUri);
                            Toast.makeText(getContext(), "Upload Done.", Toast.LENGTH_LONG).show();
                        }
                    });filepath.putFile(resultUri).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgress.dismiss();
                            Toast.makeText(getContext(), "Upload Failed.", Toast.LENGTH_LONG).show();
                        }
                    });

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }


        else if(requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
                Uri uri = data.getData();
                CropImage.activity(uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(getContext(), this);
            }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mProgress.setMessage("Uploading Image...");
                mProgress.show();

                final Uri resultUri = result.getUri();
                StorageReference filepath = mStorage.child("Photos").child(resultUri.getLastPathSegment());
                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgress.dismiss();
                        mimageView.setImageURI(resultUri);
                        Toast.makeText(getContext(), "Upload Done.", Toast.LENGTH_LONG).show();
                    }
                });filepath.putFile(resultUri).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgress.dismiss();
                        Toast.makeText(getContext(), "Upload Failed.", Toast.LENGTH_LONG).show();
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }

        }

        }



}





