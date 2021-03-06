package com.yyj.stydyroom.study.viewholder;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;




import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.constant.MemberType;
import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.ui.TViewHolder;
import com.yyj.stydyroom.base.ui.dialog.CustomAlertDialog;
import com.yyj.stydyroom.base.ui.dialog.EasyAlertDialogHelper;
import com.yyj.stydyroom.base.ui.widget.AvatarImageView;
import com.yyj.stydyroom.base.utils.NetworkUtil;
import com.yyj.stydyroom.study.adapter.OnlinePeopleAdapter;
import com.yyj.stydyroom.study.adapter.OnlinePeopleAdapter.OnlinePeopleItem;
import com.yyj.stydyroom.study.helper.ChatRoomMemberCache;
import com.yyj.stydyroom.study.helper.MsgHelper;
import com.yyj.stydyroom.study.util.MeetingOptCommand;
import com.yyj.stydyroom.views.data.MyCache;

import java.util.List;


/**
 * Created by hzxuwen on 2015/12/18.
 */
public class OnlinePeopleViewHolder extends TViewHolder {
    private static final String TAG = OnlinePeopleViewHolder.class.getSimpleName();
    private AvatarImageView avatarImageView;
    private TextView nameText;
    private OnlinePeopleItem onlinePeopleItem;
    private ImageView identityImage;
    private TextView permissionBtn;
    public ImageView volumeImage;
    private TextView interactiveText;

    private String roomId;
    private String account;

    @Override
    protected int getResId() {
        return R.layout.online_people_item;
    }

    @Override
    protected void inflate() {
        identityImage = findView(R.id.identity_image);
        avatarImageView = findView(R.id.user_head);
        nameText = findView(R.id.user_name);
        permissionBtn = findView(R.id.audio_video_btn);
        volumeImage = findView(R.id.volume_image);
        interactiveText = findView(R.id.interacting);

        setListener();
    }

    private void setListener() {
        avatarImageView.setOnClickListener(onClickListener);
        permissionBtn.setOnClickListener(onClickListener);
        avatarImageView.setOnClickListener(onClickListener);
        nameText.setOnClickListener(onClickListener);
    }

    @Override
    protected void refresh(Object item) {
        onlinePeopleItem = (OnlinePeopleItem) item;
        roomId = onlinePeopleItem.getChatRoomMember().getRoomId();
        account = onlinePeopleItem.getChatRoomMember().getAccount();

        if (onlinePeopleItem.getChatRoomMember().getMemberType() == MemberType.CREATOR) {
            identityImage.setVisibility(View.VISIBLE);
            identityImage.setImageDrawable(context.getResources().getDrawable(R.drawable.master_icon));
        } else if (onlinePeopleItem.getChatRoomMember().getMemberType() == MemberType.ADMIN) {
            identityImage.setVisibility(View.VISIBLE);
        } else {
            identityImage.setVisibility(View.GONE);
        }


        if (onlinePeopleItem.getChatRoomMember().getAccount().equals(MyCache.getAccount())) {
            // item为自己，判断是否有互动权限
            permissionBtn.setVisibility(View.GONE);
            if (ChatRoomMemberCache.getInstance().hasPermission(roomId, account)) {
                interactiveText.setVisibility(View.VISIBLE);
            } else {
                // 自己没有权限，也不显示任何操作按钮
                interactiveText.setVisibility(View.GONE);
            }
        } else {
            // item为别人的情况
            if (onlinePeopleItem.getCreator().equals(MyCache.getAccount())) {
                // 自己是主播
                    // 没举手，两种情况。正在互动中和没申请权限
                    if (ChatRoomMemberCache.getInstance().hasPermission(roomId, account)) {
                        // 正在互动中
                        beingInteractive();
                    } else {
                        permissionBtn.setVisibility(View.GONE);
                    }

            } else {
                // 自己不是主播
                permissionBtn.setVisibility(View.GONE);
                if (ChatRoomMemberCache.getInstance().hasPermission(roomId, account)) {
                    interactiveText.setVisibility(View.VISIBLE);
                } else {
                    interactiveText.setVisibility(View.GONE);
                }
            }


        }

        avatarImageView.loadAvatarByUrl(onlinePeopleItem.getChatRoomMember().getAvatar());
        nameText.setText(TextUtils.isEmpty(onlinePeopleItem.getChatRoomMember().getNick()) ? "" : onlinePeopleItem.getChatRoomMember().getNick());
    }

    // 正在互动中，按钮布局
    private void beingInteractive() {
        permissionBtn.setVisibility(View.VISIBLE);
        permissionBtn.setBackgroundResource(R.drawable.nim_red_round_button);
        permissionBtn.setText(R.string.being_interactive);
    }

    // 有人举手时，主播显示同意互动，按钮布局
    private void agreeToInteraction() {
        permissionBtn.setVisibility(View.VISIBLE);
        permissionBtn.setBackgroundResource(R.drawable.nim_blue_btn);
        permissionBtn.setText(R.string.agree_to_interaction);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!NetworkUtil.isNetAvailable(context)) {
                Toast.makeText(context, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                return;
            }

            switch (v.getId()) {
                case R.id.audio_video_btn:
                    if (ChatRoomMemberCache.getInstance().hasPermission(roomId, account)) {
                        showCancelConfirmDialog();
                    } else {
                        setAudioVideoPermission();
                    }
                    break;
                case R.id.user_head:
                case R.id.user_name:
                    showDialog();
                    break;
            }
        }
    };

    private void setAudioVideoPermission() {
        if (!ChatRoomMemberCache.getInstance().hasPermission(roomId, account)) {
            List<String> accounts = ChatRoomMemberCache.getInstance().getPermissionMembers(roomId);
            if (accounts != null && accounts.size() > 3) {
                Toast.makeText(context, "互动人数已满！", Toast.LENGTH_SHORT).show();
                return;
            }
            permissionBtn.setBackgroundResource(R.drawable.nim_red_round_button);
            permissionBtn.setText(R.string.being_interactive);


            // 同意连麦请求
            MsgHelper.getInstance().sendP2PCustomNotification(roomId, MeetingOptCommand.SPEAK_ACCEPT.getValue(), account, null);

            // 将成员图像显示到画图上
            ((OnlinePeopleAdapter) adapter).getVideoListener().onVideoOn(account);

        } else {

            permissionBtn.setVisibility(View.GONE);

            // 挂断连麦请求
            MsgHelper.getInstance().sendP2PCustomNotification(roomId, MeetingOptCommand.SPEAK_REJECT.getValue(), account, null);
            // 将成员图像从画布移除
            ((OnlinePeopleAdapter) adapter).getVideoListener().onVideoOff(account);
        }

        // 通知更新的有权限的成员列表
        MsgHelper.getInstance().sendCustomMsg(roomId, MeetingOptCommand.ALL_STATUS);
        // 设置通知红点消失
        ((OnlinePeopleAdapter) getAdapter()).getVideoListener().onTabChange(false);
    }

    private void showDialog() {
        if (!onlinePeopleItem.getCreator().equals(MyCache.getAccount())) {
            // 不是老师，没有权限
            return;
        }
        CustomAlertDialog alertDialog = new CustomAlertDialog(context);
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);

        alertDialog.addItem(R.string.kick_out_meeting, new CustomAlertDialog.onSeparateItemClickListener() {

            @Override
            public void onClick() {
                kickMember();
            }
        });
        alertDialog.show();
    }

    private void kickMember() {
        NIMClient.getService(ChatRoomService.class).kickMember(roomId, account, null).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                // 请出成功后，清除举手状态
                ((OnlinePeopleAdapter) getAdapter()).getVideoListener().onKickOutSuccess(account);
                Toast.makeText(context, R.string.kick_out_meeting_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                Log.d(TAG, "kick member failed:" + code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.d(TAG, "kick member exception:" + exception);
            }
        });
    }

    private void showCancelConfirmDialog() {
        EasyAlertDialogHelper.createOkCancelDialog(context, null, context.getString(R.string.cancel_permission_confirm),
                                                   context.getString(R.string.ok), context.getString(R.string.cancel), true,
                                                   new EasyAlertDialogHelper.OnDialogActionListener() {
                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        setAudioVideoPermission();
                    }
                }).show();
    }
}
