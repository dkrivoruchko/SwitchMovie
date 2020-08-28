package info.dvkr.switchmovie.ui.activity

import info.dvkr.switchmovie.R
import info.dvkr.switchmovie.databinding.ActivityAppBinding
import info.dvkr.switchmovie.helpers.viewBinding

class AppActivity : BaseActivity(R.layout.activity_app) {

    private val binding by viewBinding { activity -> ActivityAppBinding.bind(activity.findViewById(R.id.container)) }
}