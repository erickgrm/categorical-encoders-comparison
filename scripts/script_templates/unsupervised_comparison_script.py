#Main routine for comparison of categorical encoders for unsupervised learning
# @author: github.com/erickgrm

#########################################################
# Change the following according to the name of the file to encode
# and separation character (',' or '\t')
filename =  # example: 'adult.data' (with full path if not in same folder)
separation = '\t'

# Change the following according to where the encoders folder is
encoders_path = '../../encoders/'

# Get filename prefix
name_prefix= filename.split('.')[0]
##########################################################


# Read file and print summary
import pandas as pd
import sys
sys.path.insert(1, encoders_path)
from custom.utilities import *
dataset = pd.read_csv(filename, sep=separation, header=None) 
print('>> Total rows: ', len(dataset.iloc[0:,0]))
print('>> Number of variables:', len(dataset.iloc[0,0:])-1)
print('>> Total categorical instances:', num_categorical_instances(dataset), '\n')

# Separate target variable
features = scale_df(dataset.drop(dataset.columns[-1], axis=1))
target = dataset.iloc[:,-1]


'''START: Import encoders'''
import category_encoders as ce

from pattern_preserving_encoders import NaivePPEncoder, AgingPPEncoder, GeneticPPEncoder
from custom.utilities import *
from EEEncoder import *
from CENGEncoder import *
#from CESAMOEncoder import *


Encoders = {'Ordinal': ce.OrdinalEncoder(),
            'Polynomial': ce.PolynomialEncoder(),
            'OneHot': ce.OneHotEncoder(),
            'Backward Difference': ce.BackwardDifferenceEncoder(),
            'Helmert': ce.HelmertEncoder(),
            'Entity Embedding': EEEncoder(),
            'Target': ce.TargetEncoder(),
            'WOE': ce.WOEEncoder(),
            #'CENG': CENGEncoder(),
            #'CESAMOEncoder': CESAMOEncoder(),
            'Naive PP': NaivePPEncoder(name_estimator='LinearRegression', num_predictors=2),
            'Genetic PP': GeneticPPEncoder(name_estimator='LinearRegression', num_predictors=2),
            'Aging PP': AgingPPEncoder(name_estimator='LinearRegression', num_predictors=2)}
'''END: Import encoders'''


'''START: Import models'''

import sklearn.linear_model as lm
import sklearn.svm as svm
from sklearn.neighbors import KNeighborsClassifier
from sklearn.naive_bayes import GaussianNB
from sklearn.ensemble import RandomForestClassifier
from sklearn.neural_network import MLPClassifier
from sklearn.gaussian_process import GaussianProcessClassifier
from sklearn.gaussian_process.kernels import RBF
rbf_kernel = 1.0 * RBF(1.0)
from custom.polynomial_regression import *

Models = {'Naive Bayes': GaussianNB(),
        'Linear Regression': lm.LinearRegression(), 
        'Polynomial Regression': PolynomialRegression(max_degree=5),
        'Logistic Regression': lm.LogisticRegression(),
        'Linear SVM': svm.LinearSVC(),
        #'Polynomial SVM (d=3)': svm.SVC(kernel='polynomial', degree=3),
        'Radial SVM': svm.SVC(kernel='rbf'),
        'K-Neighbours (K=5)': KNeighborsClassifier(),
        'Random Forest (n=50)': RandomForestClassifier(n_estimators=50),
        'Neural Network': MLPClassifier(hidden_layer_sizes=(100, 50)),
        'Gaussian Process': GaussianProcessClassifier(kernel=rbf_kernel)}
'''END: Import models'''


#Main evaluation routine 
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import roc_auc_score as auc


def performance(model, encoder, K):
    accumulated_auc = 0.0
    for i in range(K):
        X_train, X_test, y_train, y_test = train_test_split(features, target, test_size=0.30)
        X_train_enc = encoder.fit_transform(X_train, y_train)

        model.fit(X_train_enc, y_train) 

        X_test_enc = encoder.transform(X_test)
        y_test_predict = model.predict(X_test_enc)
        accumulated_auc += auc(y_test, y_test_predict)

    return accumulated_auc/K

table = []

K = 3 #Meta-parameter for evaluation of encoders
for model in Models:
    model_dict = {}
    for encoder in Encoders:
        model_dict[encoder] = performance(Models[model], Encoders[encoder],K)
    table.append(model_dict)

pd.DataFrame(table, index=Models.keys()).to_latex('table_'+name_prefix+'.tex', header=None)




