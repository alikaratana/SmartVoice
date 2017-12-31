import pandas
import numpy
from sklearn.externals import joblib
from sklearn.svm import SVC
from sklearn.neighbors import KNeighborsClassifier
from sklearn.ensemble import RandomForestClassifier


# function for splitting dataset as features(x) and labels(y)
def split_dframe_x_y(dframe):
    # create a numpy array from DataFrame
    dnumpy = numpy.array(dframe)
    # get the size of the array
    size = dnumpy.shape[1]
    # get features as x
    x_dnumpy = dnumpy[:, :size - 1]
    # get labels as y
    y_dnumpy = dnumpy[:, size - 1]
    return x_dnumpy, y_dnumpy


# function that trains the model with the dataset
def train_model(model, data_set_x, data_set_y):
    model.fit(data_set_x, data_set_y)
    print("Model is trained !")


# main function
def main():
    # get dataset as DataFrame
    data_set = pandas.read_csv('dataset.csv', index_col=False)
    print("Shape of Dataset:", data_set.shape)
    print("---------------------------------")
    # split dataset into features(x) and labels(y)
    dnumpy_x, dnumpy_y = split_dframe_x_y(data_set)
    # create the model
    model = SVC(gamma=0.01, kernel='poly', degree=1)
    # model = RandomForestClassifier(n_estimators=35)
    # model = KNeighborsClassifier(n_neighbors=1)

    # train the model
    train_model(model, dnumpy_x, dnumpy_y)
    # save the model for future use
    joblib.dump(model, 'model.pkl')


if __name__ == '__main__':
    main()
