package com.cometchat.pro.uikit.ui_components.groups.banned_members;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchat.pro.core.BannedGroupMembersRequest;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.uikit.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import com.cometchat.pro.uikit.ui_components.groups.group_members.GroupMemberAdapter;
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.ClickListener;
import com.cometchat.pro.uikit.ui_resources.utils.recycler_touch.RecyclerTouchListener;

public class CometChatBanMembers extends Fragment {
    private BannedGroupMembersRequest bannedGroupMembersRequest;
    private String guid,gName;
    private TextView noMemberTv;
    private String loggedInUserScope;
    private GroupMemberAdapter groupMemberAdapter;
    private RecyclerView bannedMemberRv;
    private int LIMIT = 30;
    private GroupMember groupMember;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cometchat_ban_member, container, false);
        bannedMemberRv = view.findViewById(R.id.bannedMembers_rv);
        noMemberTv = view.findViewById(R.id.no_member_tv);
        bannedMemberRv.setLayoutManager(new LinearLayoutManager(getContext()));
        groupMemberAdapter = new GroupMemberAdapter(getContext());
        bannedMemberRv.setAdapter(groupMemberAdapter);
        handleArguments();
        getBannedMembers();
        bannedMemberRv.addOnItemTouchListener(new RecyclerTouchListener(getContext(), bannedMemberRv, new ClickListener() {
            @Override
            public void onClick(View var1, int var2) {
                GroupMember user = (GroupMember) var1.getTag(R.string.user);
                groupMember = user;
                if (loggedInUserScope!=null && (loggedInUserScope.equals(com.cometchat.pro.constants.CometChatConstants.SCOPE_ADMIN)||loggedInUserScope.equals(com.cometchat.pro.constants.CometChatConstants.SCOPE_MODERATOR))) {
                    registerForContextMenu(bannedMemberRv);
                    getActivity().openContextMenu(var1);
                }
            }
        }));
        return view;
    }
    public void handleArguments() {
        if (getArguments()!=null){
            guid = getArguments().getString(UIKitConstants.IntentStrings.GUID);
            gName = getArguments().getString(UIKitConstants.IntentStrings.GROUP_NAME);
            loggedInUserScope = getArguments().getString(UIKitConstants.IntentStrings.MEMBER_SCOPE);
        }
    }
    public void getBannedMembers() {
        if(bannedGroupMembersRequest==null)
            bannedGroupMembersRequest = new BannedGroupMembersRequest.BannedGroupMembersRequestBuilder(guid).setLimit(LIMIT).build();
        bannedGroupMembersRequest.fetchNext(new CometChat.CallbackListener<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                groupMemberAdapter.addAll(groupMembers);
                checkIfNoMember();
            }

            @Override
            public void onError(CometChatException e) {
                if (bannedMemberRv!=null)
                    Snackbar.make(bannedMemberRv,getResources().getString(R.string.ban_list_fetch_error),Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void checkIfNoMember() {
        if (groupMemberAdapter!=null && groupMemberAdapter.getItemCount()>0) {
            noMemberTv.setVisibility(View.GONE);
        } else {
            noMemberTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (getActivity()!=null) {
            MenuInflater menuInflater = getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.group_action_menu, menu);
            menu.findItem(R.id.item_ban).setTitle(getContext().getString(R.string.unban));
            menu.findItem(R.id.item_remove).setVisible(false);
            menu.findItem(R.id.item_make_admin).setVisible(false);

            menu.setHeaderTitle(getContext().getString(R.string.actions));
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.item_ban) {
            unBanMember();
        }

        return super.onContextItemSelected(item);
    }

    private void unBanMember() {
        CometChat.unbanGroupMember(groupMember.getUid(), guid, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (bannedMemberRv!=null)
                    Snackbar.make(bannedMemberRv,groupMember.getName()+" "+getResources().getString(R.string.unbanned_successfully),Snackbar.LENGTH_LONG).show();
                groupMemberAdapter.removeGroupMember(groupMember);
            }

            @Override
            public void onError(CometChatException e) {
                if (bannedMemberRv!=null)
                    Snackbar.make(bannedMemberRv,String.format(getResources().getString(R.string.unban_error),groupMember.getName()),Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
