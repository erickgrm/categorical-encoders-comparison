""" Auxiliary functions for the CENG Encoder
"""
import pandas as pd
import numpy as np
from sklearn.preprocessing import MinMaxScaler 


def replace_in_df(df, mapping): 
    """ Replaces categories by numbers according to the mapping
        If a category is not in mapping, it gets a random code
        mapping: dictionary from categories to codes
    """
    # Ensure df has the right type
    if not(isinstance(df,(pd.DataFrame))):
        try: 
            df = pd.DataFrame(df)
        except:
            raise Exception('Cannot convert to pandas.DataFrame')
            
    cat_cols = categorical_cols(df)

    # Updates the mapping with random codes for categories not 
    # previously in the mapping
    for x in cat_cols:
        cats = np.unique(df[x])
        for x in cats:
            if not(x in mapping):
                mapping[x] = np.random.uniform(0,1)

    return df.replace(mapping)


def split_str(s):
    """ Splits str + ": " + float into str, float
    """
    i=0
    while s[i] != ':':
        i+=1
    return s[:i], s[i+2:]


def codes_to_dictionary(L):
    """ L: list of strings of the form str + ": " + float
        RETURNS dictionary with elements str : float
    """
    dict = {}
    for x in L:
        k, v = split_str(x)
        dict[k] = v
    return dict

def is_categorical(array):
    """ Tests if the column is categorical
    """
    return array.dtype.name == 'category' or array.dtype.name == 'object'

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
















