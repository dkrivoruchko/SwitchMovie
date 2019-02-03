package info.dvkr.switchmovie.ui.activity

import android.os.Bundle
import info.dvkr.switchmovie.R

class AppActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)
    }
}