""" Auxiliary functions for scripts"""

import numpy as np
from sklearn.preprocessing import MinMaxScaler

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

""" Polynomial regression """
from sklearn.preprocessing import PolynomialFeatures
from sklearn.linear_model import LinearRegression

class PolynomialRegression(LinearRegression):

    def __init__(self, max_degree=3, interaction=False):
        super().__init__()
        self.max_degree = max_degree
        self.interaction = interaction
        self.poly = PolynomialFeatures(self.max_degree, interaction_only=self.interaction)

    def fit(self, X, y):
        return super(PolynomialRegression, self).fit(self.poly.fit_transform(X),y)

    def predict(self, X):
            return super(PolynomialRegression, self).predict(self.poly.fit_transform(X))

