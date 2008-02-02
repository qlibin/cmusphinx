package edu.cmu.sphinx.frontend.databranch;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Boolean;
import edu.cmu.sphinx.util.props.S4Integer;

import java.util.LinkedList;
import java.util.List;

/**
 * A buffer for <code>Data</code>-elements.
 * <p/>
 * <code>Data</code>s are inserted to the buffer using the <code>processDataFrame</code>-method.
 */
public class DataBufferProcessor extends BaseDataProcessor implements DataListener {

    private List<Data> featureBuffer = new LinkedList<Data>();

    /**
     * If this property is set <code>true</code> the buffer will wait for new data until it returns from a
     * <code>getData</code>-call. Enable this flag if the buffer should serve as starting point for a new
     * feature-pull-chain..
     */
    @S4Boolean(defaultValue = false)
    public static final String PROP_WAIT_IF_EMPTY = "waitIfEmpty";
    private boolean waitIfEmpty;

    /**
     * The time in milliseconds which will be waited between two attemtps to read a data-lement from the buffer when
     * being in <code>waitIfEmpty</code>-mode
     */
    @S4Integer(defaultValue = 10)
    public static final String PROP_WAIT_TIME_MS = "waitTimeMs";
    private long waitTime;


    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);

        waitIfEmpty = ps.getBoolean(PROP_WAIT_IF_EMPTY);

        if (waitIfEmpty) // if false we don't need the value
            waitTime = ps.getInt(PROP_WAIT_TIME_MS);
    }


    public void processDataFrame(Data data) {
        featureBuffer.add(data);
    }


    /**
     * Returns the processed Data output.
     *
     * @return an Data object that has been processed by this DataProcessor
     * @throws edu.cmu.sphinx.frontend.DataProcessingException
     *          if a data processor error occurs
     */
    public Data getData() throws DataProcessingException {
        Data data = null;

        while (waitIfEmpty && featureBuffer.isEmpty()) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!featureBuffer.isEmpty()) {
            data = featureBuffer.remove(0);
        }

        return data;
    }


    public int getBufferSize() {
        return featureBuffer.size();
    }


    public void clearBuffer() {
        featureBuffer.clear();
    }
}
