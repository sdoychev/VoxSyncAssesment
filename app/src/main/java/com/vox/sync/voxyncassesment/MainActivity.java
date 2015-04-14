package com.vox.sync.voxyncassesment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends ActionBarActivity {

    private TextView inputCaption, bitmapCaption;
    private ImageView bitmapView;
    private Button loadImageButton, drawTextButton;
    private Button blackButton, blueButton, redButton, greenButton, closeButton;
    private Bitmap bitmap;
    private View.DragShadowBuilder captionShadow;
    private int closeButtonVisibility = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControls();
        setListeners();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Return back to activity and check if result was ok
        if (requestCode == Constants.LOAD_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                // Recycle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }
                //Create the bitmap
                InputStream stream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                //Load the bitmap
                bitmapView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initControls() {
        //Initialize the controls in current activity
        loadImageButton = (Button) findViewById(R.id.loadImageBtn);
        inputCaption = (TextView) findViewById(R.id.inputCaption);
        drawTextButton = (Button) findViewById(R.id.drawTextOnBitmap);
        bitmapView = (ImageView) findViewById(R.id.bitmapView);
        blackButton = (Button) findViewById(R.id.colorBtnBlack);
        blueButton = (Button) findViewById(R.id.colorBtnBlue);
        redButton = (Button) findViewById(R.id.colorBtnRed);
        greenButton = (Button) findViewById(R.id.colorBtnGreen);
        closeButton = (Button) findViewById(R.id.closeCaptionBtn);
        bitmapCaption = (TextView) findViewById(R.id.bitmapCaption);
        //Set tags for views
        bitmapCaption.setTag(Constants.CAPTION_TAG);
    }

    private void setListeners() {
        loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to Open Image applications like Gallery, Google Photos, etc.
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Set selected type to be images only
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Start activity for result - return back to current app with selected image
                startActivityForResult(intent, Constants.LOAD_IMAGE_REQUEST_CODE);
            }
        });

        drawTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputCaption.getText().length() >= 1) {
                    if (bitmap != null) {
                        //Get text colors and content
                        bitmapCaption.setText(inputCaption.getText());
                        bitmapCaption.setTextColor(inputCaption.getCurrentTextColor());
                        if (View.VISIBLE != bitmapCaption.getVisibility()) {
                            bitmapCaption.setX(bitmapView.getTop() + bitmapView.getWidth() / 2 - getBitmapCaptionWidth() / 2);
                            bitmapCaption.setY(bitmapView.getTop() + bitmapView.getHeight() / 2 - bitmapCaption.getHeight() / 2);
                        }
                        bitmapCaption.setVisibility(View.VISIBLE);
                        showCloseButton();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getText(R.string.select_image), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.enter_text), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Color buttons, text changes on click
        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCaptionColor(getResources().getColor(R.color.custom_black));
            }
        });

        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCaptionColor(getResources().getColor(R.color.custom_blue));
            }
        });

        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCaptionColor(getResources().getColor(R.color.custom_red));
            }
        });

        greenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCaptionColor(getResources().getColor(R.color.custom_green));
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapCaption.setVisibility(View.INVISIBLE);
                closeButton.setVisibility(View.INVISIBLE);
            }
        });

        //Caption can be moved after Long click
        bitmapCaption.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activateDrag(v, bitmapCaption);
                closeButtonVisibility = closeButton.getVisibility();
                closeButton.setVisibility(View.INVISIBLE);
                return true;
            }
        });

        //Create and set the drag event listener for the View
        bitmapView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        if (closeButtonVisibility == 0) {
                            closeButton.setVisibility(View.VISIBLE);
                        } else {
                            closeButton.setVisibility(View.INVISIBLE);
                        }
                        break;
                    case DragEvent.ACTION_DROP:
                        //Get position and size of the caption and bitmap holder.
                        float x_coord = event.getX();
                        float y_coord = event.getY();
                        int bitmapHolderTopLimit = bitmapCaption.getTop();
                        int bitmapHolderRightLimit = bitmapView.getRight();
                        int bitmapHolderLeftLimit = bitmapView.getLeft();
                        int bitmapHolderBottomLimit = bitmapView.getBottom();
                        int captionWidth = bitmapCaption.getWidth();
                        int captionHeight = bitmapCaption.getHeight();
                        //The caption cannot go outside of the bitmap holder.
                        if (x_coord < bitmapHolderLeftLimit + captionWidth / 2) {
                            x_coord = bitmapHolderLeftLimit + captionWidth / 2;
                        } else if (x_coord > bitmapHolderRightLimit - captionWidth / 2) {
                            x_coord = bitmapHolderRightLimit - captionWidth / 2 - closeButton.getWidth() / 2;
                        }
                        if (y_coord < bitmapHolderTopLimit + captionHeight / 2) {
                            y_coord = bitmapHolderTopLimit + captionHeight / 2;
                        } else if (y_coord > bitmapHolderBottomLimit - captionHeight / 2) {
                            y_coord = bitmapHolderBottomLimit - captionHeight / 2;
                        }
                        bitmapCaption.setX(x_coord - captionWidth / 2);
                        bitmapCaption.setY(y_coord - captionHeight / 2);
                        //Set the position for the Close button
                        showCloseButton();
                    case DragEvent.ACTION_DRAG_ENDED:
                        break;
                    default:
                        break;
                }
                inputCaption.setVisibility(View.VISIBLE);
                return true;
            }
        });
    }

    private int getBitmapCaptionWidth() {
        return bitmapCaption.getText().length() * Constants.LETTER_SIZE_DP + bitmapCaption.getPaddingLeft() * 2;
    }

    private void showCloseButton() {
        //Position the close button in top right corner and make it visible
        closeButton.setX(bitmapCaption.getX() + getBitmapCaptionWidth() - closeButton.getWidth() / 2);
        closeButton.setY(bitmapCaption.getY() - closeButton.getHeight() / 2);
        closeButton.setVisibility(View.VISIBLE);
    }

    private void setCaptionColor(int color) {
        inputCaption.setTextColor(color);
    }

    private void activateDrag(View view, TextView textView) {
        ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData dragData = new ClipData(view.getTag().toString(), mimeTypes, item);
        //Instantiates the drag shadow builder.
        captionShadow = new View.DragShadowBuilder(textView);
        //Start the drag
        view.startDrag(dragData, captionShadow, null, 0);
    }
}
