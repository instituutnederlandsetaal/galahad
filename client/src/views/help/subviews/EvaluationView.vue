<template>
    <div class="flex" style="padding: 0 !important;">
        <GCard headless noHelp class="left">
            <h2>Contents</h2>
            <ul>
                <li>
                    <a href="#metrics">Metrics</a>
                </li>
                <li>
                    <a href="#false-negative">False Negative (FN)</a>
                </li>
                <li>
                    <a href="#false-positive">False Positive (FP)</a>
                </li>
                <li>
                    <a href="#true-positive">True Positive (TP)</a>
                </li>
                <li>
                    <a href="#true-negative">True Negative (TN)</a>
                </li>
                <li>
                    <a href="#precision">Precision</a>
                </li>
                <li>
                    <a href="#recall">Recall</a>
                </li>
                <li>
                    <a href="#difference-precision-recall">Difference precision and recall</a>
                    <ul>
                        <li><a href="#diff-high-precision-low-recall">High precision, low recall</a></li>
                        <li><a href="#diff-low-precision-high-recall">Low precision, high recall</a></li>
                    </ul>
                </li>
                <li>
                    <a href="#f1">F1</a>
                    <ul>
                        <li><a href="#f1-high-precision-low-recall">High precision, low recall</a></li>
                        <li><a href="#f1-low-precision-high-recall">Low precision, high recall</a></li>
                    </ul>
                </li>
                <li>
                    <a href="#micro-macro">Macro-averaging versus micro-averaging</a>
                    <ul>
                        <li><a href="#macro">Macro-averaging</a></li>
                        <li><a href="#micro">Micro-averaging</a></li>
                        <li><a href="#choosing-micro-macro">Choosing micro-averaging or macro-averaging</a></li>
                    </ul>
                </li>
                <li>
                    <a href="#accuracy">Accuracy</a>
                    <ul>
                        <li><a href="#binary-accuracy">Label-specific accuracy (binary accuracy)</a></li>
                        <li><a href="#corpus-level-accuracy">Corpus-level accuracy</a></li>
                    </ul>
                </li>
                <li>
                    <a href="#no-match">No match</a>
                </li>
                <li>
                    <a href="#multiple-pos">Multiple part-of-speech</a>
                </li>

            </ul>
        </GCard>
        <div class="right">
            <GCard headless noHelp class="right">
                <!-- H1: On Evaluation -->
                <h1 id="evaluation">On Evaluation: A Detailed Overview</h1>

                <!-- H2: Metrics -->
                <h2 id="metrics">Metrics</h2>
                <p>
                    In annotation, False Negatives (FN), False Positives (FP), True Negatives (TN), and True Positives
                    (TP)
                    are concepts that indicate whether a label of interest was present (positive) or absent (negative),
                    and
                    whether the prediction was correct (true) or incorrect (false) about the presence or absence. These
                    metrics are discussed in detail below but in short they can be summarised as follows, where a
                    prediction
                    (e.g. the predicted part-of-speech tag) is different from the true part-of-speech tag.
                </p>
                <p>
                    These metrics are always considered “from the viewpoint” of a specific label, as the examples below
                    will
                    make clear. You’ll notice that False Negatives, False Positives and True Positives can all be
                    explained
                    from the perspective of either the predicted or the correct tag. True Negatives are a bit different
                    to
                    think of (example 3) because the viewpoint of a different tag is taken (neither the correct nor the
                    predicted label but any other tag). This makes True Negatives a bit more abstract and often less
                    emphasised in tagging tasks (it is not included in GaLAHaD) but it is included here for the sake of
                    completeness.
                </p>

                <p>
                    <b>Example 1:</b> word <i>liep</i> (English: <i>ran</i>), correct label “verb” ↔ predicted label
                    “noun”
                </p>
                <ul>
                    <li>From the viewpoint of “verb”: <b>False Negative</b>: the prediction is “not a verb” (negative)
                        and
                        that
                        prediction is incorrect (false)</li>
                    <li>From the viewpoint of “noun”: <b>False Positive</b>: the prediction is “noun” (positive) but
                        that
                        prediction is incorrect (false)</li>
                </ul>

                <p>
                    <b>Example 2:</b> word <i>kasteel</i> (English: <i>castle</i>), correct label “noun” = predicted
                    label
                    “noun”
                </p>
                <ul>
                    <li>From the viewpoint of “noun”: <b>True Positive</b>: the prediction is “noun” (positive) and that
                        prediction
                        is correct (true)</li>
                </ul>

                <p>
                    <b>Example 3:</b> word <i>snel</i> (English: <i>quickly</i>), correct label “adverb” = predicted
                    label
                    “adverb”
                </p>
                <ul>
                    <li>
                        From the viewpoint of “noun”: <b>True Positive:</b> the prediction is “adverb” (positive) and
                        that
                        prediction is correct (true)</li>
                    <li>
                        From the viewpoint of “verb”: <b>False Negative:</b> the prediction is “not a verb” (negative)
                        and
                        that
                        prediction is correct (true)
                    </li>
                </ul>

                <p>
                    So in your analyses you should always consider which perspective (or which label) is important to
                    you.
                </p>

                <p>
                    As will be discussed, these calculations can then be used to calculate metrics such as Precision,
                    Recall, F1 score and accuracy.
                </p>

                <p>
                    The metrics are explained in detail below but as a summary (where X can be e.g. “verb” or “noun”):
                </p>

                <ul>
                    <li><b>Pre</b>cision: out of all <b>pre</b>dictions for X, how many are indeed actual X</li>
                    <li><b>Re</b>call: out of all <b>re</b>ference labels X, how many were correctly predicted</li>
                    <li>F1: a balance between precision and recall</li>
                    <li>Accuracy: out of all tokens, how many were correctly predicted as not-X or X</li>
                </ul>

                <!-- H2: False Negative (FN) -->
                <h2 id="false-negative">False Negative (FN)</h2>

                <p>
                    A False Negative (FN) is an annotation that should have been annotated with a label (e.g. X) but is
                    not.
                    The
                    predicted label is not (“negative”) X, and that prediction is incorrect (“false”).
                </p>
                <p>
                    In part-of-speech tagging, this means that a word should have received a certain grammatical label
                    (let's
                    call it label X, such as "verb"), but it wasn't marked as such by the tagger. In the reference layer
                    (the
                    correct answer key), the word has label X, but in the hypothesis layer (a tagger’s prediction), it
                    is
                    wrongly labelled as something else.
                </p>
                <p>
                    <b>Example:</b> The Dutch word <i>liep</i> (past tense of <i>lopen</i>, meaning 'to walk') is not
                    correctly tagged as a verb but
                    as a noun. So the “verb” annotation for <i>liep</i> is a false negative because the prediction was
                    not a
                    verb but a
                    noun and that is incorrect.
                </p>

                <!-- H2: False Positive (FP) -->
                <h2 id="false-positive">False Positive (FP)</h2>
                <p>A False Positive (FP) occurs when a label X is incorrectly assigned to a word. The predicted label is
                    X
                    (“positive”), and that prediction is incorrect (“false”).</p>
                <p>So a word was tagged with a particular grammatical label (e.g. "verb"), but it shouldn’t have been.
                    In
                    the hypothesis layer, the word is incorrectly given label X, while the reference layer shows it
                    should
                    have a different label.</p>
                <p><b>Example:</b> The tagger tags the word <i>het</i> (meaning 'the' in English) as a verb, which is
                    incorrect
                    because
                    <i>het</i> is actually a determiner. So the “verb” annotation for <i>het</i> is a false positive
                    because
                    the
                    prediction was “verb”.
                </p>

                <!-- H2: True Positive (TP) -->
                <h2 id="true-positive">True Positive (TP)</h2>
                <p>A True Positive (TP) signifies a correct annotation, where a word is accurately labelled with a
                    category
                    X. The predicted label is X (“positive”), and that prediction is correct (“true”).</p>
                <p>True Positives occur when the tagger successfully predicts the correct grammatical label (such as
                    "verb")
                    to a word. Both the hypothesis layer (a tagger’s prediction) and the reference layer (the correct
                    answer
                    key) agree that the word is a verb.</p>
                <p><b>Example:</b> The Dutch word <i>liep</i> was correctly identified as a verb, matching the reference
                    layer’s
                    labelling.</p>

                <!-- H2: True Negative (TN) -->
                <h2 id="true-negative">True Negative (TN)</h2>
                <p>A True Negative (TN) refers to the correct identification that a word should not be labelled X. The
                    predicted label is not X (“negative”), and that prediction is correct (“true”).</p>
                <p>A True Negative indicates that a word was correctly tagged with a label different from the label X
                    being
                    tagged with an incorrect grammatical label (let’s consider label X) when it shouldn’t be. Both the
                    hypothesis layer (a tagger’s prediction) and the reference layer (the correct answer key) correctly
                    show
                    that the word does not have label X.</p>
                <p><b>Example:</b> The tool correctly tags <i>koek</i> (meaning ‘cookie’) as a noun, so from the
                    perspective
                    of the
                    label
                    “verb” this prediction is a true negative: the label was correctly not classified as a verb.</p>

                <!-- H2: Precision -->
                <h2 id="precision">Precision</h2>
                <p>To give an example in the same atmosphere as our GaLAHaD tool for historical Dutch: imagine you are
                    an
                    archer shooting arrows at a target. Precision (P) means how many of your arrows, out of all the ones
                    you
                    shot, actually hit the bullseye. In terms of tagging, precision tells us how many of the annotations
                    in
                    the hypothesis layer are actually correct. The focus lies on the prediction (<b>pre</b>cision
                    &mdash;
                    <b>pre</b>diction).
                </p>
                <p>Mathematically, precision is calculated using the formula:</p>
                <p><code>P = TP / (TP+FP)</code></p>
                <p>So precision is the ratio of the correct prediction (TP), e.g. “verb”, and all predictions “verb”,
                    including those “verb” predictions that are incorrect (TP+FP). In other words, Precision wants the
                    false
                    positives (incorrect prediction of “verb”) to be low.</p>
                <p><b>Example:</b> Suppose our tagger identified 100 tokens with the “verb” label (TP+FP). However, only
                    87
                    of
                    these predictions are correct (TP). So the precision is 87% (87+100). This suggests that 87 out of
                    100
                    times, our tagger's "arrows" hit the target, accurately labelling verbs.</p>
                <p>On the corpus level (Precision across all instances), Precision can be calculated with
                    micro-averaging or
                    macro-averaging, where different approaches are taken on combining the precision of all possible
                    labels
                    into one final score. This is described in the section on “Macro-averaging versus micro-averaging”.
                </p>

                <!-- H2: Recall -->
                <h2 id="recall">Recall</h2>
                <p>Rather than an archer, now imagine you're an adept spy tasked with spotting enemy soldiers in the
                    night.
                    The goal is to count all of the soldiers. Recall (R) is analogous to how many soldiers you
                    successfully
                    identify compared to all the ones that are actually hiding in the woods. Just like the spy aiming to
                    spot every enemy, recall aims to determine how well our tagger identifies all the words that truly
                    belong to a particular grammatical category. It's a measure of thoroughness in detection of all the
                    right answers. So the focus lies on the references (<b>re</b>call &mdash; <b>re</b>ference).</p>
                <p>Recall can be formulated as follows:</p>
                <p><code>R = TP / (TP+FN)</code></p>
                <p>So recall is the ratio of the correct prediction (TP), e.g. “verb”, and all the occurrences of “verb”
                    in
                    the references, including the False Negatives that were not predicted by the tagger (TP+FN). Put
                    differently, Recall emphasises that false negatives should be low (“verb” tokens that were
                    incorrectly
                    labelled as something else).</p>
                <p><b>Example:</b> in our reference layer, we see that the label “verb” occurs 100 times (TP+FN).
                    However,
                    the tagger only tagged 64 as “verb” (TP) and did not spot the other 36 (FN). Recall is therefore
                    64%.
                </p>
                <p>To aggregate Recall on the corpus level to achieve one total Recall score for the whole corpus, the
                    Recall of all possible labels can be averaged. Typically two approaches are distinguished,
                    macro-averaging and micro-averaging, described in the section on “Macro-averaging versus
                    micro-averaging”.</p>

                <!-- H2: Difference precision and recall -->
                <h2 id="difference-precision-recall">Difference precision and recall</h2>
                <p>Let’s consider two example scenarios that exemplify the difference between precision and recall. In
                    all
                    cases our dataset consists of 100 tokens and our label of interest is “verb”.</p>
                <h3 id="diff-high-precision-low-recall">High precision, low recall</h3>
                <ul>
                    <li>TP (correctly labelled as verb): 10</li>
                    <li>FP (incorrectly labelled as verb): 1</li>
                    <li>FN (incorrectly labelled as not a verb): 15</li>
                    <li>TN (correctly labelled as not a verb): 74</li>
                </ul>
                <p>We can notice the difference between the number of FP and FN: the tagger only once made a mistake in
                    predicting “verb” where it should not, but it made 15 mistakes tagging a token as a non-verb where
                    it
                    should have been a verb. We calculate precision and recall as illustrated in the previous sections:
                </p>
                <ul>
                    <li><code>P = TP / (TP+FP) = 10 / (10+1) = 90.9%</code></li>
                    <li><code>R = TP / (TP+FN) = 10 / (10+15) = 40%</code></li>
                </ul>
                <p>Despite a high precision, the tagger’s recall for the tag “verb” is low. When it <b>pre</b>dicted the
                    label
                    “verb” it was most often correct (high <b>pre</b>cision) but out of all cases where “verb” was the
                    correct
                    <b>re</b>ference, it missed a lot of verbs (low <b>re</b>call). This occurs when a tagger is not
                    making
                    a lot of
                    “verb” predictions (so it misses quite a bit of the verbs) but the “verb” prediction that it does
                    make
                    are often correct.
                </p>
                <h3 id="diff-low-precision-high-recall">Low precision, high recall</h3>
                <ul>
                    <li>TP (correctly labelled as verb): 20</li>
                    <li>FP (incorrectly labelled as verb): 30</li>
                    <li>FN (incorrectly labelled as not a verb): 5</li>
                    <li>TN (correctly labelled as not a verb): 45</li>
                </ul>
                <p>In contrast to the previous example, we now see that the model predicted the label “verb” when it
                    should
                    not be a verb 30 times (FP) and predicted a verb when it should have been a verb (FN) only 5 times.
                    This
                    will lead to a difference in precision and recall:</p>
                <ul>
                    <li><code>P = TP / (TP+FP) = 20 / (20+30) = 40%</code></li>
                    <li><code>R = TP / (TP+FN) = 20 / (20+5) = 80%</code></li>
                </ul>
                <p>In contrast to the previous example, the tagger now identifies most verbs (high recall) but it
                    “overshoots”; it incorrectly labelled many tokens as “verb” that should have been something else
                    (low
                    precision). This happens when the tagger assigns a lot of “verb” tags and by doing so indeed finds
                    the
                    verbs in the text but also misclassifies many other words as verb.</p>

                <!-- H2: F1 -->
                <h2 id="f1">F1</h2>
                <p>While the archer and the spy mentioned above both have their skills and uses, a master strategist is
                    able
                    to master both of their strengths. Similarly, the F1 score is a combination of Precision and Recall
                    to
                    provide a balanced view of a tagger’s performance, accounting for how accurate its predictions are
                    as
                    well as for how many of the references it actually got right.</p>
                <p>Mathematically, the F1 score is calculated as the harmonic mean of Precision and Recall, formulated
                    as:
                </p>
                <p><code>F1 = 2 * (P * R) / (P + R)</code></p>
                <p>Or, using true/false and positive/negative terminology:</p>
                <p><code>F1 = 2TP / (2TP + FP + FN)</code></p>
                <p>Combining Precision and Recall in this way strikes a balance between predicting accurately
                    (Precision)
                    and predicting thoroughly (Recall). A tagger must score well on both facets to achieve a high F1
                    score.
                    To illustrate this, we can calculate the F1 score for the two scenarios above (high precision and
                    low
                    recall vs. low precision and high recall).</p>
                <h3 id="f1-high-precision-low-recall">High precision, low recall:</h3>
                <ul>
                    <li><code>P = TP / (TP+FP) = 10 / (10+1) = 90.9%</code></li>
                    <li><code>R = TP / (TP+FN) = 10 / (10+15) = 40%</code></li>
                    <li><code><b>F1</b> = 2 * (P * R) / (P + R) = 2 * (0.909 * 0.40) / (0.909 + 0.40) = 0.553&hellip;</code>
                    </li>
                </ul>
                <h3 id="f1-low-precision-high-recall">Low precision, high recall</h3>
                <ul>
                    <li><code>P = TP / (TP+FP) = 20 / (20+30) = 40%</code></li>
                    <li><code>R = TP / (TP+FN) = 20 / (20+5) = 80%</code></li>
                    <li><code><b>F1</b> = 2 * (P *R) / (P + R) = 2 * (0.40 * 0.80) / (0.40 + 0.80) = 0.533&hellip;</code>
                    </li>
                </ul>
                <p>From these examples it should be clear that very different precision and recall values can lead to
                    similar F1 scores. Depending on your interest, it can therefore still be useful to look at precision
                    and/or recall separately!</p>

                <!-- H2: Macro-averaging versus micro-averaging -->
                <h2 id="micro-macro">Macro-averaging versus micro-averaging</h2>
                <p>When we aggregate the precision, recall and F1 to retrieve a single corpus level score rather than
                    individual scores for each class (“verb”, “noun”, etc.), we can average all the label scores. There
                    are
                    different ways to average these label scores with both their own uses. Macro-averaging focuses on
                    the
                    highest level, to assign equal weight to all classes. Micro-averaging instead looks at the samples
                    under
                    a microscope, where each individual instance is assigned equal weight.</p>
                <h3 id="macro">Macro-averaging</h3>
                <p>Once more relying on an example in a historical setting, we can consider macro-averaging as the case
                    of
                    captains in the army. Within the army, the opinion of each captain is equally important regardless
                    of
                    how many soldiers they lead. Similarly, in macro-averaging a high frequency class like “verb” that
                    has
                    many occurrences is weighed the same as a lower frequency class like an interjection &mdash; the
                    tagger’s
                    performance on verbs is considered equally important to its performance on interjections.</p>
                <p>Mathematically, the macro-average for Precision, Recall, and F1 are &mdash; intuitively &mdash; the
                    averages of the
                    P, R and F1 for each class (e.g. precision for “verb”, precision for “noun”, precision for
                    “interjection”, etc.):</p>
                <ul>
                    <li><code>P_macro = sum of all class Ps / number of classes</code></li>
                    <li><code>R_macro = sum of all class Rs / number of classes</code></li>
                    <li><code>F1_ macro = sum of all class F1s / number of classes</code></li>
                </ul>
                <p>By giving equal weight to all classes, regardless of their frequency, the aggregated scores give an
                    intuition of how a tagger performs “on average” across all classes, although this average may be
                    highly
                    skewed when the model performs very differently on some of the classes.</p>
                <h3 id="micro">Micro-averaging</h3>
                <p>Unlike macro-averaging, where a captain’s voice (class) is given equal importance regardless of its
                    number of soldiers, micro-averaging does not consider the captains themselves but instead focuses on
                    the
                    individual soldiers. Each individual soldier gets its own voice, and each voice is measured equally.
                    In
                    tagging, that means that all instances are accounted for, which means that more frequent labels like
                    “verb” <i>will</i> impact the score: classes that occur more frequently will have a stronger impact
                    on
                    the
                    final score.</p>
                <p>The micro-average can be calculated by means of the analysis of the FN, FP, TP, TN discussed above:
                </p>
                <ul>
                    <li><code>P_micro = sum of all TP / sum of all (TP+FP)</code></li>
                    <li><code>R_micro = sum of all TP / sum of all (TP+FN)</code></li>
                    <li><code>F1_ micro= 2 * (P_micro * R_micro) / (P_micro + R_micro)</code></li>
                </ul>
                <p>Emphasising the individual importance of each sample implies that majority classes will be more
                    impactful
                    on the final score. This approach can be useful when you are more interested in the real
                    distribution of
                    classes, where you indeed want to prioritize high frequency classes to weigh more thoroughly and you
                    are
                    less interested in minority classes.</p>
                <h3 id="choosing-micro-macro">Choosing micro-averaging or macro-averaging</h3>
                <p><b>Macro-averaging</b> is useful when you care about the model's ability to perform well across each
                    class (e.g. PoS tag) independently. It is helpful if you’re interested in an even performance in
                    every
                    class, regardless of the class frequency.</p>
                <p><b>Micro-averaging</b> is better suited when you want to evaluate the overall performance across all
                    instances (tokens), especially if there is class imbalance where some tags are more frequent than
                    others. For example, if verbs and nouns are overwhelmingly more frequent than other tags,
                    micro-averaging will provide a performance measure that is highly impacted by these high-frequency
                    classes.</p>

                <!-- H2: Accuracy -->
                <h2 id="accuracy">Accuracy</h2>
                <p>Accuracy provides a simple, intuitive way of interpreting a tagger’s performance. It comes down to
                    “how
                    many of the predictions were correct”, or in GaLAHaD terminology: how many of the predictions in the
                    prediction layer are identical to the ones in the reference layer.</p>
                <h3 id="binary-accuracy">Label-specific accuracy (binary accuracy)</h3>
                <p>Accuracy can be calculated for an individual label like “verb”, in which case we consider a binary
                    case:
                    out of all tokens, which ones were correctly identified as a “verb” and correctly identified as “not
                    a
                    verb”. Mathematically, that is the ratio of true positive and true negatives, and the number of
                    tokens.
                </p>
                <p><code>Acc_binary = (TP+TN) / (Total=TP+FP+TN+FN)</code></p>
                <p><b>Example:</b> Precision, Recall and Accuracy for “verb”</p>

                <EvaluationExampleCorpus />

                <p>Consider the table above, where the tagger has made some predictions. To calculate binary accuracy on
                    “verb”, we first calculate the positives and negatives:</p>

                <ul>
                    <li>TP (correctly predicted verbs): <i>spring, speelde</i> = 2</li>
                    <li>FP (incorrectly predicted as verbs): 0</li>
                    <li>FN (incorrectly not predicted as verbs): <i>lopen</i> = 1</li>
                    <li>TN (correctly predicted as not a verb): remaining words = 7</li>
                </ul>

                <p>These numbers allow us to calculate Precision and Recall as before, and now also include accuracy.
                </p>

                <ul>
                    <li><code>P = TP / (TP + FP) = 2 / (2 + 0) = 1.0</code></li>
                    <li><code>R = TP / (TP + FN) = 2 / (2 + 1) = 0.666&hellip;</code></li>
                    <li><code>Acc = (TP + TN) / Total = (2 + 7) / 10 = 0.9</code></li>
                </ul>

                <p>So out of the two predictions “verb” that were made, both of those two were indeed correct
                    (Precision).
                    Out of the three references that should have been “verb”, only two were found (Recall). And in all
                    predictions the tagger correctly identified whether a tag is a verb or “not a verb” except for
                    <i>lopen</i>
                    which it incorrectly classified as a noun (binary accuracy).
                </p>
                <h3 id="corpus-level-accuracy">Corpus-level accuracy</h3>
                <p>Using binary accuracy to see whether a tag was correctly predicted is useful, but accuracy is used
                    more
                    often on the corpus level to get a more intuitive idea of the tagger’s performance. Accuracy is
                    considered less nuanced but more intuitive than Precision, Recall and F1 score because it only
                    considers
                    whether a given tag matches the reference or not. It is easy to calculate and easy to understand:
                    how
                    many of the predictions were actually correct?</p>

                <p><code>Acc = correct predictions / number of tokens</code></p>

                <p>This formulation of accuracy is similar to <b>micro-averaging</b> because the <i>classes</i> are not
                    given equal weight
                    but instead <i>all predictions</i> are counted as equal, like each individual soldier in the army in
                    the
                    examples above.</p>

                <p><b>Example:</b> Precision, Recall and Accuracy for the whole corpus</p>

                <EvaluationExampleCorpus />

                <p>While calculating corpus-level accuracy is easy (simply the proportion where the labels in the
                    “predicted” column are the same as the “actual” column), Precision and Recall (and therefore F1) do
                    require us to first calculate the positives and negatives for each label. In this dummy corpus we
                    only
                    have three labels: verb, noun and adjective.</p>

                <p><i>Verb:</i></p>
                <ul>
                    <li>TP (correctly predicted verbs): <i>spring, speelde</i> = 2</li>
                    <li>FP (incorrectly predicted as verbs): 0</li>
                    <li>FN (incorrectly not predicted as verbs): <i>lopen</i> = 1</li>
                    <li>TN (correctly predicted as not a verb): remaining words = 7</li>
                </ul>


                <ul>
                    <li><code><b>P</b> = TP / (TP + FP) = 2 / (2 + 0) = 1.0</code></li>
                    <li><code><b>R</b> = TP / (TP + FN) = 2 / (2 + 1) = 0.666&hellip;</code></li>
                    <li><code><b>Acc</b> = (TP + TN) / Total = (2 + 7) / 10 = 0.9</code></li>
                </ul>

                <p><i>Noun:</i></p>
                <ul>
                    <li>TP (correctly predicted nouns): <i>hond, katten, vos</i> = 3</li>
                    <li>FP (incorrectly predicted as nouns): <i>lopen, lui</i> = 2</li>
                    <li>FN (incorrectly not predicted as nouns): 0</li>
                    <li>TN (correctly predicted as not a nouns): remaining words = 5</li>
                </ul>


                <ul>
                    <li><code><b>P</b> = TP / (TP + FP) = 3 / (3 + 2) = 0.6</code></li>
                    <li><code><b>R</b> = TP / (TP + FN) = 3 / (3 + 0) = 1.0</code></li>
                    <li><code><b>Acc</b> = (TP + TN) / Total = (3 + 5) / 10 = 0.8</code></li>
                </ul>

                <p><i>Adjective:</i></p>
                <ul>
                    <li>TP (correctly predicted adjectives): <i>snel, mooie, blij</i> = 3</li>
                    <li>FP (incorrectly predicted as adjectives): 0</li>
                    <li>FN (incorrectly not predicted as adjectives): <i>lui</i> = 1</li>
                    <li>TN (correctly predicted as not a adjectives): remaining words = 6</li>
                </ul>


                <ul>
                    <li><code><b>P</b> = TP / (TP + FP) = 3 / (3 + 0) = 1.0</code></li>
                    <li><code><b>R</b> = TP / (TP + FN) = 3 / (3 + 1) = 0.75</code></li>
                    <li><code><b>Acc</b> = (TP + TN) / Total = (3 + 6) / 10 = 0.9</code></li>
                </ul>

                <p>All in all our tagger does its job quite well! <b>
                        We can look at different metrics depending on what our
                        focus is:
                    </b></p>
                <ul>
                    <li><b>Pre</b>cision: out of all <b>pre</b>dictions for X, a number of them were indeed actual X
                    </li>
                    <li><b>Re</b>call: out of all <b>re</b>ference labels X, a number of them were correctly predicted
                    </li>
                    <li>Accuracy: out of all tokens, a number of them were correctly predicted as not-X or X</li>
                </ul>

                <p>Now that we calculated the class-specific scores, we can aggregate them into single scores for the
                    whole
                    corpus. For the sake of ease we will use <b>macro-averaging</b> for Precision and Recall here.</p>

                <ul>
                    <li><code><b>P</b> = sum of all class Ps / number of classes = (1.0 + 0.6 + 1.0) / 3 = 0.866&hellip;</code>
                    </li>
                    <li><code><b>R</b> = sum of all class Rs / number of classes = (0.66&hellip; + 1.0 + 0.75) / 3 = 0.8055&hellip;</code>
                    </li>
                    <li><code><b>Acc</b> = number of correct predictions / number of tokens = 8 / 10 = 0.8</code></li>
                </ul>

                <p>Note here that macro-averaging has smoothed out the lower <code>P=0.6</code> for nouns! The high
                    precision of verbs
                    and adjectives is now masking the lower precision of nouns.</p>
                <h2 id="no-match">No match</h2>
                <p><i>No match</i> counts the number of instances for which there was an issue with the alignment of
                    hypothesis and
                    reference layer, so a predicted label could not be successfully mapped to a reference (so no score
                    could
                    be calculated).</p>
                <h2 id="multiple-pos">Multiple part-of-speech</h2>
                <p>For tokens that in fact consist of more than one word, a multiple analysis is given. This means that
                    one
                    token is not only assigned more than one lemma but also more than one part of speech.</p>
                <p><b>Example:</b> token <i>“int”</i> analysed as IN (ADP) + HET (PD) (English: in it)</p>
                <p>The evaluation results for part-of-speech tagging are taking the assignment of multiple
                    part-of-speech
                    tags into account.</p>

            </GCard>
        </div>
    </div>
</template>

<script setup>
import { GCard } from '@/components'
import EvaluationExampleCorpus from './EvaluationExampleCorpus.vue';
</script>


<style scoped>
img {
    display: block;
    width: 100%;
    margin: 0 auto;
}

p.red {
    color: red;
}

h3,
h4 {
    font-weight: bold;
}

.right :deep(.content-wrapper)>.content {
    flex: 0 1 800px !important;
}

code {
    font-size: 16px;

    &.green-marker {
        background-color: var(--int-green);
        padding: 0.2rem 0.3rem;
    }
}

a,
a:hover,
a:active,
a:visited {
    color: #000;
}

.flex {
    display: flex !important;
    flex-direction: row !important;
    overflow: hidden !important;
}

.right {
    overflow-y: auto;
}

.left {
    flex: 1;
    border-right: 1px solid var(--int-light-grey);
    padding-bottom: 0;
    overflow-y: auto;


    h2 {
        margin-bottom: -0.3rem;
    }

    ul {
        padding-left: 2rem;
        margin-bottom: 0;
    }

    ul ul {
        padding-left: 1rem;
    }

    li {
        margin: 0.3rem 0;
    }
}

@media (max-width: 800px) {
    .flex {
        flex-direction: column !important;
    }

    .left {
        border-right: none;
        display: block;

        ul ul {
            padding-left: 2rem;
        }
    }

    :deep(.left) .content-wrapper {
        justify-content: initial;
    }
}

.tabs.level-2>.content {
    padding-bottom: 0em !important;
}
</style>