package me.selinali.tribble.ui.deck;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wenchao.cardstack.CardStack;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.selinali.tribble.ArchiveManager;
import me.selinali.tribble.R;
import me.selinali.tribble.api.Dribble;
import me.selinali.tribble.model.Shot;
import me.selinali.tribble.ui.Bindable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static me.selinali.tribble.TribbleApp.unsubscribe;

public class DeckFragment extends Fragment implements Bindable<List<Shot>> {

  public DeckFragment() {
  }

  public static Fragment newInstance() {
    return new DeckFragment();
  }

  private static final int PRELOAD_THRESHOLD = 5;

  @BindView(R.id.card_stack) CardStack mCardStack;

  private Subscription mSubscription;
  private Unbinder mUnbinder;
  private DeckAdapter mAdapter;
  private int mCurrentPage = 1;

  private DeckListener mDeckListener = new DeckListener() {
    @Override
    void onCardSwiped(int direction, int swipedIndex) {
      if (mAdapter.getCount() - swipedIndex == PRELOAD_THRESHOLD) {
        mCurrentPage++;
        loadNext();
      }

      if (direction == RIGHT) {
        ArchiveManager.instance().archive(mAdapter.getItem(swipedIndex));
      } else if (direction == LEFT) {
        ArchiveManager.instance().discard(mAdapter.getItem(swipedIndex));
      }
    }
  };

  private void loadNext() {
    unsubscribe(mSubscription);
    mSubscription = Dribble.instance().getShots(mCurrentPage)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::bind, Throwable::printStackTrace);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    loadNext();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_deck, container, false);
    mUnbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mUnbinder.unbind();
    unsubscribe(mSubscription);
  }

  @Override
  public void bind(List<Shot> shots) {
    if (mAdapter == null) {
      mAdapter = new DeckAdapter(getContext(), shots);
      mCardStack.setListener(mDeckListener);
      mCardStack.setAdapter(mAdapter);
    } else {
      mAdapter.addAll(shots);
    }
  }
}