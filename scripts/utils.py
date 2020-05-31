""" Auxiliary functions for scripts"""

import numpy as np
from sklearn.preprocessing import MinMaxScaler


def scale_df(df):
    """ Scale all numerical variables to [0,1]
    """
    numerical_cols = [x for x in list(df.columns) if x not in categorical_cols(df)]
    sc = MinMaxScaler()

    for x in numerical_cols:
        df[x] = sc.fit_transform(df[x].values.reshape(-1,1))
    return df

def is_categorical(series):
    """ Tests if the column of a dataframe is categorical
    """
    return series.dtype.name == 'category' or series.dtype.name == 'object'

def categorical_cols(df):
    """ Return the column numbers of the categorical variables in df
    """
    cols = []
    # Rename columns as numbers
    df.columns = range(len(df.columns))

    for x in df.columns:
        if is_categorical(df[x]):
            cols.append(x)
    return cols

def num_categorical_cols(df):
    """ Returns the number of categorical variables """
    return len(categorical_cols(df))

def categorical_instances(df):
    """ Returns an array with all the categorical instances in df
    """
    instances = []
    cols = categorical_cols(df)
    for x in cols:
        instances = instances + list(np.unique(df[x]))
    return instances

def num_categorical_instances(df):
    """ Returns the total number of categorical instances in df
    """
    return len(categorical_instances(df))
