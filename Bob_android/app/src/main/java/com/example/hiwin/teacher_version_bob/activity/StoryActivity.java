package com.example.hiwin.teacher_version_bob.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.hiwin.teacher_version_bob.R;
import com.example.hiwin.teacher_version_bob.StoryAdapter;
import com.example.hiwin.teacher_version_bob.fragment.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class StoryActivity extends BluetoothCommunicationActivity {

    private JSONObject selectedObject;

    @Override
    protected void receive(byte[] data) {
        String str = new String(data, StandardCharsets.UTF_8);
        Log.d(getClass().getSimpleName(), str);
        try {
            JSONObject obj = new JSONObject(str);
            String content = obj.getString("content");
            if (content.equals("all_stories_info")) {
                JSONArray array = obj.getJSONArray("data");
                Fragment fragment = getSelectFragment(array, null, null);
                postFragment(fragment, "stories select");
            } else if (content.equals("story_content")) {
                JSONObject dataObj = obj.getJSONObject("data");
//                showStory(dataObj.getJSONArray("pages"));
                showVocabulary(dataObj.getJSONArray("vocabularies"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Toolbar getToolbar() {
        return (Toolbar) findViewById(R.id.story_toolbar);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_story;
    }

    @Override
    protected void initialize(Bundle savedInstanceState) {
    }

    @Override
    protected String getDeviceAddress(Bundle savedInstanceState) {
        Intent it = getIntent();
        return it.getStringExtra("address");
    }

    @Override
    protected void onConnect() {
        sendMessage("STORY_GET LIST");

    }

    @Override
    protected void onDisconnect() {

    }

    private void showStory(JSONArray pages) throws JSONException {
        Fragment previous = null;
        for (int i = pages.length() - 1; i >= 0; i--) {
            JSONObject page = pages.getJSONObject(i);
            previous = getStoryPageFragment(page, previous, "page" + (i + 1));
        }
        postFragment(previous, "page0");
    }

    private void showVocabulary(JSONArray vocabularies) throws JSONException {
        Fragment previous = null;
        for (int i = vocabularies.length() - 1; i >= 0; i--) {
            JSONObject vocabulary = vocabularies.getJSONObject(i);
            previous = getVocabularyFragment(vocabulary, previous, "vocabulary" + (i + 1));
        }
        postFragment(previous, "vocabulary0");
    }

    private Fragment getSelectFragment(JSONArray array, Fragment next, String nextId) {
        StaticFragment selectFragment = new StoriesSelectFragment();
        selectFragment.setShowListener(views -> ((ListView) views[0]).setAdapter(new StoryAdapter(this, array)));
        selectFragment.setListener(new StoriesSelectFragment.ItemSelectListener() {

            @Override
            public void onItemSelected(int position) {
                try {
                    selectedObject = array.getJSONObject(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void start() {

            }

            @Override
            public void end() {
                try {
                    String id = selectedObject.getString("id");
                    Toast.makeText(StoryActivity.this, id, Toast.LENGTH_SHORT).show();
                    sendMessage("STORY_GET STORY " + id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return selectFragment;
    }

    private Fragment getVocabularyFragment(JSONObject vocabulary, Fragment next, String nextId) throws JSONException {

        VocabularyFragment storyPageFragment = new VocabularyFragment();
        final Drawable drawable = getDrawable(getResourceIDByString(vocabulary.getString("image"), "raw"));
        String name = vocabulary.getString("name");
        String translated = vocabulary.getString("translated");
        String part_of_speech = vocabulary.getString("part_of_speech");

//        final MediaPlayer player;
//        player = MediaPlayer.create(StoryActivity.this, getResourceIDByString(page.getString("audio"), "raw"));

        storyPageFragment.setShowListener(views -> {
            ((ImageView) views[0]).setImageDrawable(drawable);
            ((TextView) views[1]).setText(name+" ("+part_of_speech+".)");
            ((TextView) views[2]).setText(translated);
        });

        storyPageFragment.setListener(new FragmentListener() {
            @Override
            public void start() {
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        end();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
//                player.start();
//                player.setOnCompletionListener(mp -> end());
            }

            @Override
            public void end() {
                postFragment(next, nextId);
            }
        });
        return storyPageFragment;
    }

    private Fragment getStoryPageFragment(JSONObject page, Fragment next, String nextId) throws JSONException {
        StoryPageFragment storyPageFragment = new StoryPageFragment();

        final Drawable drawable;
        final String text;
        final MediaPlayer player;
        drawable = getDrawable(getResourceIDByString(page.getString("image"), "raw"));
        text = page.getString("text");
        player = MediaPlayer.create(StoryActivity.this, getResourceIDByString(page.getString("audio"), "raw"));

        storyPageFragment.setShowListener(views -> {
            ((ImageView) views[0]).setImageDrawable(drawable);
            ((TextView) views[1]).setText(text);
        });

        storyPageFragment.setListener(new FragmentListener() {
            @Override
            public void start() {
                player.start();
                player.setOnCompletionListener(mp -> end());
            }

            @Override
            public void end() {
                postFragment(next, nextId);
            }
        });
        return storyPageFragment;
    }

    private void postFragment(Fragment fragment, String id) {
        if (fragment == null || id == null)
            return;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.replace(R.id.story_frame, fragment, id);
        fragmentTransaction.commit();
    }

    private int getResourceIDByString(String resName, String type) {
        return getApplicationContext().getResources()
                .getIdentifier(resName
                        , type
                        , getPackageName());
    }

}
