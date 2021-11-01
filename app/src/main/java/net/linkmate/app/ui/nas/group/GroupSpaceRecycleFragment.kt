package net.linkmate.app.ui.nas.group

import android.Manifest
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.weline.repo.api.ERROR_40207
import io.weline.repo.api.V5HttpErrorNo
import io.weline.repo.api.V5_ERR_SESSION_EXP
import kotlinx.android.synthetic.main.fragment_safe_box_nas_file.*
import libs.source.common.livedata.Resource
import libs.source.common.livedata.Status
import libs.source.common.utils.RateLimiter
import net.linkmate.app.R
import net.linkmate.app.ui.nas.TipsBaseFragment
import net.linkmate.app.ui.nas.group.list.*
import net.linkmate.app.ui.nas.widget.OnCheckedChangeListener
import net.linkmate.app.ui.nas.widget.SortMenuPopupViewV2
import net.linkmate.app.util.Dp2PxUtils
import net.linkmate.app.util.ToastUtils
import net.sdvn.cmapi.CMAPI
import net.sdvn.nascommon.constant.OneOSAPIs
import net.sdvn.nascommon.db.GroupsKeeper
import net.sdvn.nascommon.fileserver.constants.SharePathType
import net.sdvn.nascommon.iface.Callback
import net.sdvn.nascommon.model.*
import net.sdvn.nascommon.model.oneos.BaseResultModel
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.OneOSFileManage
import net.sdvn.nascommon.model.oneos.OneOSFileType
import net.sdvn.nascommon.model.oneos.tansfer_safebox.SafeBoxDownloadManager
import net.sdvn.nascommon.model.oneos.transfer.DownloadElement
import net.sdvn.nascommon.model.oneos.transfer.TransferManager
import net.sdvn.nascommon.model.oneos.vo.FileListModel
import net.sdvn.nascommon.model.phone.LocalFileType
import net.sdvn.nascommon.utils.FileUtils
import net.sdvn.nascommon.utils.PermissionChecker
import net.sdvn.nascommon.utils.ToastHelper
import net.sdvn.nascommon.widget.FileManagePanel
import net.sdvn.nascommon.widget.FileSelectPanel
import net.sdvn.nascommon.widget.TypePopupView
import net.sdvn.nascommon.widget.badgeview.QBadgeView
import org.view.libwidget.anim.BezierViewHolder
import org.view.libwidget.setOnRefreshWithTimeoutListener
import java.util.*
import java.util.concurrent.TimeUnit


//保险箱展示列表
class GroupSpaceRecycleFragment : TipsBaseFragment() {

    private var loadFilesFormServer: LiveData<Resource<BaseResultModel<FileListModel>>>? = null
    private val mSafeBoxFileListModel by viewModels<GroupSpaceNasFileModel>({ requireParentFragment() })
    private var mNowPath = OneOSFileType.RECYCLE.serverTypeName
    private var page = 0
    private val mRateLimiter = RateLimiter<Any>(1500, TimeUnit.MILLISECONDS) //点击加间隔
    lateinit var mSafeBoxNasFileAdapter: GroupSpaceNasFileAdapter
    private lateinit var mTransferView: View
    private val navArgs by navArgs<GroupSpaceRecycleFragmentArgs>()
    override fun getLayoutResId(): Int {
        return R.layout.fragment_safe_box_nas_file
    }


    override fun initView(view: View) {
        mNowPath = navArgs.rootPath ?: mNowPath
        initTitle()
        initMiddle()
        initRecycleView()
        initAddPopu()
        initSelect()//这个是选择蓝相关
        fab.visibility=View.GONE
        mTipsBar = tipsBar
    }

    override fun getTopView(): View? {
        return title_bar
    }

    private lateinit var mQBadgeView: QBadgeView

    //下面是标题头部分
    private fun initTitle() {
        //这里是设置标题头
        title_bar.setBackListener {
            onBackPressed()
        }
        when (navArgs.sharePathType) {
            SharePathType.USER.type -> {
                title_bar.setTitleText(R.string.file_type_private)
            }
            SharePathType.PUBLIC.type -> {
                title_bar.setTitleText(R.string.file_type_public)
            }
            SharePathType.SAFE_BOX.type -> {
                title_bar.setTitleText(R.string.root_dir_name_safe_box)
            }
            SharePathType.GROUP.type -> {
                title_bar.setTitleText(R.string.group)
            }
        }
        initStatusBarPadding(layout_select_top_panel)
    }

    private var prefixName:String?=null
    //中间展示的部分
    private fun initMiddle() {
        if (navArgs.sharePathType == SharePathType.GROUP.type && navArgs.groupid > 0) {
            prefixName=   GroupsKeeper.findGroup(
                    navArgs.deviceid,
                    navArgs.groupid
                )?.name
        }
        setNowPath(mNowPath)
        layout_path_panel.showOrderButton(true)
        layout_path_panel.showNewFolderButton(false)
        layout_path_panel.setOnPathPanelClickListener { v, path ->
            if (v.id == R.id.ibtn_order) {
                showOrderPopView(v)
            } else if (!path.isNullOrEmpty()) {
                val pathStr = if (path.startsWith(OneOSAPIs.ONE_OS_SAFE_ROOT_DIR)) {
                    path.replaceFirst(
                        OneOSAPIs.ONE_OS_SAFE_ROOT_DIR,
                        OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR
                    )
                } else if (path.startsWith(OneOSAPIs.ONE_OS_GROUP_ROOT_DIR)) {
                    path.replaceFirst(
                        OneOSAPIs.ONE_OS_GROUP_ROOT_DIR,
                        OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR
                    )
                } else {
                    path
                }
                if (mNowPath != pathStr) {
                    mNowPath = pathStr
                    setNowPath(mNowPath)
                    reload()
                }
            }
        }
    }


    var popup: SortMenuPopupViewV2? = null
    private var mOrderType = FileOrderTypeV2.time_desc
    private var isListShown = true
    private fun showOrderPopView(view: View) {
        if (popup == null) {
            popup = SortMenuPopupViewV2(
                view.context,
                mOrderType,
                if (isListShown) FileViewerType.LIST else FileViewerType.GRID
            ).apply {
                setOnOrderCheckedChangeListener(object : OnCheckedChangeListener<FileOrderTypeV2> {
                    override fun onCheckedChangeListener(orderType: FileOrderTypeV2) {
                        if (orderType != mOrderType) {
                            mOrderType = orderType
                            reload()
                            dismiss()
                        }
                    }
                })
                setOnViewerCheckedChangeListener(object : OnCheckedChangeListener<FileViewerType> {
                    override fun onCheckedChangeListener(type: FileViewerType) {
                        if (isListShown != (type == FileViewerType.LIST)) {
                            isListShown = !isListShown
                            val data = mSafeBoxNasFileAdapter.data
                            initSafeBoxNasFileAdapter(isListShown)
                            mSafeBoxNasFileAdapter.setNewData(data)
                        }
                        dismiss()
                    }
                })
            }
        }
        popup!!.showAsDropDown(view, Dp2PxUtils.dp2px(requireContext(), 16), 0)
        popup!!.isOutsideTouchable = true
        popup!!.isFocusable = true
        popup!!.update()
        view.isSelected = true
        popup!!.setOnDismissListener {
            view.isSelected = false
        }
    }

    override fun onBackPressed(): Boolean {
        if (mIsSelectModel) {
            mSafeBoxNasFileAdapter.selectAll(false)
            setSelectVisibility(false)
        } else {
            if(mNowPath==OneOSFileType.RECYCLE.serverTypeName)
            {
                findNavController().popBackStack()
                return true
            }
            val parentPath = FileUtils.getParentPath(mNowPath)
            if (!TextUtils.isEmpty(parentPath) && parentPath != mNowPath) {
                mNowPath = parentPath!!
                setNowPath(mNowPath)
                reload()
            } else {
                findNavController().popBackStack()
            }

        }
        return true
    }


    private fun setNowPath(path: String) {
        layout_path_panel.updatePath(OneOSFileType.RECYCLE, path,prefixName)
    }

    private val mCompleteListener by lazy {//用LIVEDATA可能会掉数据
        TransferManager.OnTransferCompleteListener<DownloadElement> { isDownload, transferElement ->
            if (isResumed)
                mSafeBoxNasFileAdapter.onDownloadFinish(transferElement)
        }
    }


    //展示列表部分
    private fun initRecycleView() {
        initSafeBoxNasFileAdapter(isListShown)
        swipe_refresh_layout.setOnRefreshWithTimeoutListener(SwipeRefreshLayout.OnRefreshListener {
            reload()
            setSelectVisibility(false)
            swipe_refresh_layout.isRefreshing = true
        }, 1000)
        swipe_refresh_layout.isEnabled = true
        reload()
        SafeBoxDownloadManager.addTransferCompleteListener(mCompleteListener)
    }

    private fun initSafeBoxNasFileAdapter(isList: Boolean) {
        setSelectVisibility(false)
        mSafeBoxNasFileAdapter = GroupSpaceNasFileAdapter()
        mSafeBoxNasFileAdapter.groupId = navArgs.groupid
        val emptyView = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_empty_directory, null)
        mSafeBoxNasFileAdapter.emptyView = emptyView
        mSafeBoxFileListModel.mSessionLiveData.observe(this, Observer {
            mSafeBoxNasFileAdapter.mLoginSession = it
        })
        mSafeBoxNasFileAdapter.setOnItemClickListener { baseQuickAdapter, view, position ->
            val item = baseQuickAdapter.getItem(position)
            item?.let { it1 ->
                if (mRateLimiter.shouldFetch(it1)) {
                    if (mIsSelectModel) {
                        mSafeBoxNasFileAdapter.changeSelectItem(position)
                        return@setOnItemClickListener
                    }
                    if (item is OneOSFile) {
                        if (item.isDirectory()) {//如果是文件夹
                            mNowPath = item.getPath()
                            setNowPath(mNowPath)
                            mSafeBoxNasFileAdapter.setNewData(mutableListOf())//清除当前 这里可以考虑显示为正在加载
                            reload()
                        } else {
                            FileUtils.openOneOSFile(
                                mSafeBoxNasFileAdapter.mLoginSession!!,
                                requireActivity(),
                                view,
                                position,
                                mSafeBoxNasFileAdapter.data,
                                OneOSFileType.GROUP,
                                navArgs.groupid
                            )
                        }
                    }
                }
            }
        }

        mSafeBoxNasFileAdapter.setOnItemLongClickListener { baseQuickAdapter, view, position ->
            mSafeBoxNasFileAdapter.changeSelectItem(position)
            true
        }
        mSafeBoxNasFileAdapter.mSelectLiveData.observe(this, Observer {
            if (it.isNotEmpty()) {
                setSelectVisibility(true)
            }
            updateSelect(it)
        })
        mSafeBoxNasFileAdapter.setOnLoadMoreListener({ loadMore() }, recycle_view)//加载更多数据
        if (isList) {
            mSafeBoxNasFileAdapter.setListShow()
            recycle_view.layoutManager = LinearLayoutManager(context)
            recycle_view.adapter = mSafeBoxNasFileAdapter
        } else {
            mSafeBoxNasFileAdapter.setGridShow()
            recycle_view.layoutManager = GridLayoutManager(context, 4)
            recycle_view.adapter = mSafeBoxNasFileAdapter
        }
    }


    private fun reload() {
        page = 0
        loadFiles()
    }

    private fun loadFiles() {
        devId?.let {
            if (loadFilesFormServer != null) {
                loadFilesFormServer!!.removeObserver(observerReload)
            }
            loadFilesFormServer = mSafeBoxFileListModel.loadFilesFormServer(
                it,
                mNowPath,
                page = page,
                sharePathType = navArgs.sharePathType,
                orderTypeV2 = mOrderType,
                groupId = navArgs.groupid
            )
            loadFilesFormServer?.observe(this@GroupSpaceRecycleFragment, observerReload)
        }
    }


    private val observerReload = Observer<Resource<BaseResultModel<FileListModel>>> {
        if (it.status == Status.SUCCESS) {
            val data = it.data
            if (data?.isSuccess == true) {
                val data1 = data.data
                val files = data1.files ?: mutableListOf<OneOSFile>()
                if (data1.page == 0) {
                    mSafeBoxNasFileAdapter.setNewData(files)
                } else {
                    mSafeBoxNasFileAdapter.addData(files)
                }
                if (data1.hasMorePage()) {
                    page++
                    mSafeBoxNasFileAdapter.setEnableLoadMore(true)
                } else {
                    mSafeBoxNasFileAdapter.setEnableLoadMore(false)
                }
                mSafeBoxNasFileAdapter.loadMoreComplete()
            } else {
                if (data?.error?.code == ERROR_40207 || data?.error?.code == V5_ERR_SESSION_EXP) {
                    ToastUtils.showToast(V5HttpErrorNo.getResourcesId(data?.error?.code))
                }
            }
        }
    }

    private fun loadMore() {
        loadFiles()
    }


    //弹出窗口部分it
    private fun initAddPopu() {
        //TODO 在这里设置菜单权限
        fab.setOnClickListener {
            val floatingActionButton = it as FloatingActionButton
            if (mUploadPopView?.isShow == true) {
                floatingActionButton.show()
                mUploadPopView!!.dismiss()
            } else {
                floatingActionButton.show()
                mUploadPopView!!.showPopupTop2(title_bar!!)
                floatingActionButton.hide()
                mUploadPopView!!.setOnDismissListener { floatingActionButton.show() }
            }
        }
    }

    //文件类型集
    private val mUploadTypeList by lazy {
        ArrayList<FileTypeItem>().apply {
            val pic = FileTypeItem(
                R.string.file_type_pic,
                R.drawable.icon_device_img_new, 0, LocalFileType.PICTURE
            )
            add(pic)
            val video = FileTypeItem(
                R.string.file_type_video,
                R.drawable.icon_device_vedio_new, 0, LocalFileType.VIDEO
            )
            add(video)
            val audio = FileTypeItem(
                R.string.file_type_audio,
                R.drawable.icon_device_music_new, 0, LocalFileType.AUDIO
            )
            add(audio)
            val doc = FileTypeItem(
                R.string.file_type_doc,
                R.drawable.icon_device_doc_new, 0, LocalFileType.DOC
            )
            add(doc)
            val all = FileTypeItem(
                R.string.file_type_all,
                R.drawable.icon_device_folder_new, 0, LocalFileType.PRIVATE
            )
            add(all)
            val moveInFile = FileTypeItem(
                R.string.move_in_file,
                R.drawable.ic_move_in, 0, LocalFileType.MOVE_IN
            )
            add(moveInFile)
            val addItem = FileTypeItem(
                R.string.action_new_folder,
                R.drawable.icon_device_newfolder_new, 0, LocalFileType.NEW_FOLDER
            )
            add(addItem)
        }
    }

    private val mUploadPopView by lazy {
        TypePopupView(
            requireActivity(),
            mUploadTypeList,
            R.string.please_select_file_type_upload
        ).apply {
            setOnItemClickListener { parent, view, position, id ->
                mSafeBoxFileListModel.mSessionLiveData.value?.let {
                    val item = mUploadTypeList.get(position)
                    when (item.flag) {
                        LocalFileType.NEW_FOLDER -> {
                            showNewFolder()
                        }
                        LocalFileType.MOVE_IN -> {
                            moveInFolder()
                        }
                        else -> {
                            gotoUpload(item.flag as LocalFileType)
                        }
                    }
                    dismiss()
                }
            }
        }
    }

    fun showNewFolder() {
        val fileManage = OneOSFileManage(
            requireActivity(),
            null,
            mSafeBoxFileListModel.mSessionLiveData.value!!,
            null,
            OneOSFileManage.OnManageCallback {
                if (it) {
                    reload()
                }
            },
            navArgs.groupid
        )
        val path = if (TextUtils.isEmpty(mNowPath)) "/" else mNowPath
        fileManage?.manage(FileManageAction.MKDIR, path, SharePathType.GROUP.type)
    }

    fun moveInFolder() {
        findNavController().navigate(
            R.id.action_list_to_move_in,
            GroupSpaceMoveInFragmentArgs(devId!!, navArgs.groupid, mNowPath).toBundle()
        )
    }

    fun gotoUpload(localFileType: LocalFileType) {
        PermissionChecker.checkPermission(requireContext(), Callback {
            context.runCatching {
                jumpToUpload(requireContext(), localFileType, mNowPath)
            }
        }, Callback {
            context.run {
                UiUtils.showStorageSettings(this)
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE)
    }


    private fun jumpToUpload(context: Context, finalFileType: LocalFileType, curPath: String) {
        //TODO 上传的路径
        mSafeBoxFileListModel.fileType = finalFileType
        findNavController().navigate(
            R.id.action_list_to_upload,
            GroupSpaceUploadNavFragmentArgs(
                devId!!,
                PathTypeCompat.getAllStrPath(
                    sharePathType = navArgs.sharePathType,
                    tarPath = curPath
                )!!,
                navArgs.groupid
            ).toBundle()
        )
        mUploadPopView.dismiss()
    }


    lateinit var mOnFileManageListener: FileManagePanel.OnFileManageListener<OneOSFile>
    lateinit var mOnFileSelectListener: FileSelectPanel.OnFileSelectListener

    private fun initSelect() {
        GroupsKeeper.findGroup(navArgs.deviceid, navArgs.groupid)?.perm?.let {
            layout_file_manage_panel.setPerm(it)
        }
        mOnFileManageListener = object : FileManagePanel.OnFileManageListener<OneOSFile> {
            override fun onClick(
                view: View,
                list: MutableList<OneOSFile>,
                action: FileManageAction?
            ) {
                val selectedList = mutableListOf<OneOSFile>()
                selectedList.addAll(list)
                if (selectedList.isNullOrEmpty()) {
                    ToastHelper.showToast(R.string.tip_select_file)
                } else if (action != FileManageAction.MORE &&
                    action != FileManageAction.BACK &&
                    !CMAPI.getInstance().isConnected
                ) {
                    ToastHelper.showToast(R.string.network_not_available)
                    return
                }
                when (action) {
                    FileManageAction.MOVE -> {
                        moveOrCopy(selectedList, GroupSpaceMoveToFragment.MOVE)
                    }
                    FileManageAction.COPY -> {
                        moveOrCopy(selectedList, GroupSpaceMoveToFragment.COPY)
                    }
                    FileManageAction.DOWNLOAD -> {
                        downloadFile(selectedList, view, action)
                    }
                    else -> {
                        getOneOSFileManage(true).manage(OneOSFileType.GROUP, action, selectedList!!)
                    }
                }

            }

            override fun onDismiss() {


            }
        }
        layout_file_manage_panel.setOnOperateListener(mOnFileManageListener)
        mOnFileSelectListener =
            object : FileSelectPanel.OnFileSelectListener {
                override fun onSelect(isSelectAll: Boolean) {
                    mSafeBoxNasFileAdapter.selectAll(isSelectAll)
                }

                override fun onDismiss() {
                    mSafeBoxNasFileAdapter.selectAll(false)
                    setSelectVisibility(false)
                }
            }
        layout_select_top_panel.setOnSelectListener(mOnFileSelectListener)

    }


    private fun getOneOSFileManage(
        refreshData: Boolean = false,
        callback: OneOSFileManage.OnManageCallback? = null
    ): OneOSFileManage {
        val manageCallback = callback ?: OneOSFileManage.OnManageCallback {
            if (it) {
                setSelectVisibility(false)
                mSafeBoxNasFileAdapter.selectAll(false)
                if (refreshData)
                    reload()//默认操作成功后刷新数据
            }
        }
        return OneOSFileManage(
            requireActivity(),
            null,
            mSafeBoxFileListModel.mSessionLiveData.value!!,
            null,
            manageCallback,
            navArgs.groupid
        )
    }

    private var mIsSelectModel = false
    private fun setSelectVisibility(boolean: Boolean) {
        mIsSelectModel = boolean
        if (layout_file_manage_panel.visibility != View.VISIBLE && boolean) {
            layout_file_manage_panel.visibility = View.VISIBLE
        } else if (layout_file_manage_panel.visibility == View.VISIBLE && !boolean) {
            layout_file_manage_panel.visibility = View.GONE
        }

        if (layout_select_top_panel.visibility != View.VISIBLE && boolean) {
            layout_select_top_panel.visibility = View.VISIBLE
        } else if (layout_select_top_panel.visibility == View.VISIBLE && !boolean) {
            layout_select_top_panel.visibility = View.GONE
        }
    }


    private fun updateSelect(selectedList: List<OneOSFile>) {
        layout_file_manage_panel.updatePanelItems(
            OneOSFileType.RECYCLE,
            selectedList,
            mSafeBoxFileListModel.mSessionLiveData.value,
            true
        )
        layout_select_top_panel.updateCount(mSafeBoxNasFileAdapter.data.size, selectedList.size)
    }


    private fun moveOrCopy(selectedList: MutableList<OneOSFile>, type: String) {

        val list = Array(selectedList.size) { it -> "" }
        for ((index, oneOSFile) in selectedList.withIndex()) {
            list[index] = oneOSFile.getPath()
        }

        findNavController().navigate(
            R.id.action_list_to_move_to,
            GroupSpaceMoveToFragmentArgs(
                deviceid = devId!!,
                type = type,
                groupid = navArgs.groupid,
                paths = list
            ).toBundle()
        )
    }

    private fun downloadFile(
        selectedList: MutableList<OneOSFile>,
        view: View,
        action: FileManageAction
    ) {
        //开始view
        val outLocation = IntArray(2)
        view.getLocationInWindow(outLocation)
        val startPoint = Point(outLocation[0], outLocation[1])
        //结束view
        val outLocation2 = IntArray(2)
        mTransferView.getLocationInWindow(outLocation2)
        val endPoint = Point(outLocation2[0], outLocation2[1])
        val bezierViewHolder = BezierViewHolder(view.context).apply {
            setStartPosition(startPoint)
            setEndPosition(endPoint)
            files_root.addView(this)
            text = selectedList?.count().toString()
        }
        bezierViewHolder.startBezierAnimation()
        getOneOSFileManage().manage(OneOSFileType.GROUP, action, selectedList!!)
    }


    //下面是返回事件处理
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!onBackPressed()) {
                isEnabled = false
                requireActivity().onBackPressed()
                isEnabled = true
            }
        }
    }

}